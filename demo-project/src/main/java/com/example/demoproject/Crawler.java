package com.example.demoproject;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletionStage;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.pf.PFBuilder;
import akka.stream.IOResult;
import akka.stream.alpakka.file.javadsl.Directory;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import io.vavr.Tuple;
import io.vavr.Tuple2;

public class Crawler {

    public static void main(String[] args) {
        var isProperty = new PFBuilder<String,Tuple2<String,String>>()
            .match(String.class, s -> s.contains("="), s -> {
                var idx = s.indexOf("=");
                return Tuple.of(s.substring(0, idx), s.substring(idx + 1));
            })
            .build();

        var system = ActorSystem.create();

        // We only want txt or properties files
        var matcher = FileSystems.getDefault().getPathMatcher("glob:**.{txt,properties}");

        Source<Path, NotUsed> source = Directory.walk(Paths.get("."))
            .filter(matcher::matches);

        Source<Source<String, CompletionStage<IOResult>>, NotUsed> nestedContentsAsLines = source.map(p -> {

            // Read the content of the file
            Source<ByteString, CompletionStage<IOResult>> content = FileIO.fromPath(p);

            Source<String, CompletionStage<IOResult>> asLines = content
                // Split the ByteString stream whenever we encounter a newline
                .via(Framing.delimiter(ByteString.fromInts('\n'), 256, FramingTruncation.ALLOW)

                 // And turn the resulting bytes into String by interpreting them as UTF-8
                .map(b -> b.utf8String()));

            return asLines;
        });

        // Read at most 32 files concurrently, and gather all strings into one stream
        Source<String, NotUsed> contentsStream = nestedContentsAsLines.flatMapMerge(32, s -> s);

        // We only want lines that are property declarations
        Source<Tuple2<String, String>, NotUsed> props = contentsStream.collect(isProperty);

        // Let's print them for now
        try {
            props.runWith(Sink.foreach(t -> System.out.println(t)), system)
                .toCompletableFuture().get();
        } catch (Exception x ) {
            x.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}
