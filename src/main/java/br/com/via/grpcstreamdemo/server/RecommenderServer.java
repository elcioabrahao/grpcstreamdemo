package br.com.via.grpcstreamdemo.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class RecommenderServer  {
    public static final int RECOMMENDER_SERVICE_PORT = 5554;

    public static void main(String[] args)
            throws IOException, InterruptedException {
        Server server =
                ServerBuilder.forPort(RECOMMENDER_SERVICE_PORT)
                        .addService(new RecommenderServiceImpl())
                        .build();
        server.start();
        System.out.println("Servidor Recommender ONLINE!");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}