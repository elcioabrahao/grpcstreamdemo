package br.com.via.grpcstreamdemo.server;

import br.com.via.grpcstreamdemo.common.Movie;
import br.com.via.grpcstreamdemo.recommender.RecommenderRequest;
import br.com.via.grpcstreamdemo.recommender.RecommenderResponse;
import br.com.via.grpcstreamdemo.recommender.RecommenderServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class RecommenderServiceImpl extends
        RecommenderServiceGrpc.RecommenderServiceImplBase {
    @Override
    public StreamObserver<RecommenderRequest>
    getRecommendedMovie(StreamObserver<RecommenderResponse>
                                responseObserver) {
        StreamObserver<RecommenderRequest> streamObserver =
                new StreamObserver<RecommenderRequest>() {
                    List<Movie> movies = new ArrayList<>();

                    public void onNext(RecommenderRequest value) {
                        movies.add(value.getMovie());
                    }

                    public void onError(Throwable t) {
                        responseObserver.onError(Status.INTERNAL
                                .withDescription("Internal server error")
                                .asRuntimeException());
                    }

                    public void onCompleted() {
                        if (movies.size() > 0) {
                            responseObserver
                                    .onNext(RecommenderResponse.newBuilder()
                                            .setMovie(findMovieForRecommendation(movies))
                                            .build());
                            responseObserver.onCompleted();
                        } else {
                            responseObserver
                                    .onError(Status.NOT_FOUND
                                            .withDescription("Sorry, found no movies to recommend!").asRuntimeException());
                        }
                    }
                };
        return streamObserver;
    }

    private Movie findMovieForRecommendation(List<Movie> movies) {
        int random = new SecureRandom().nextInt(movies.size());
        return movies.stream().skip(random).findAny().get();
    }
}