package org.example;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class StreamStarterApp {

    public static void main(String[] args) {
        System.out.println("Hello World");

        Properties config = new Properties();
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-app");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());


        StreamsBuilder streamsBuilder = new StreamsBuilder();

        KStream<String, String> stream  =  streamsBuilder.stream("word-count-input");

        KTable<String, Long> wordCounts =  stream.mapValues(value -> value.toLowerCase())
                .flatMapValues(lowercaseValue -> Arrays.asList(lowercaseValue.split(" ")))
                .selectKey((ignoredKey, word) -> word)
                .groupByKey()
                .count(Named.as("Counts"));


        wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), config);
        kafkaStreams.start();

        System.out.println(kafkaStreams.toString());

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }
}
