package com.quantori.chem_query_platform_demo;


import akka.actor.ActorSystem;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AskPattern;
import com.quantori.qdp.core.configuration.AkkaClusterProvider;
import com.quantori.qdp.core.configuration.ClusterConfigurationProperties;
import com.quantori.qdp.core.configuration.LocalClusterProvider;
import com.quantori.qdp.core.source.SourceRootActor;
import com.quantori.qdp.core.task.TaskServiceActor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Arrays;

/**
 * Configuration class for the application using Akka, Spring Boot, and Slick.
 * Sets up the actor system, clustering, and database session for task management.
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class ActorConfiguration {

    /**
     * Creates and configures the Akka Actor system with clustering capabilities.
     *
     * @param maxAmountOfSearchActors Maximum number of search actors allowed.
     * @param hostName                Hostname for the Akka cluster.
     * @param port                    Port for the Akka cluster.
     * @param nodes                   Comma-separated list of cluster seed nodes.
     * @param applicationName         Application name used for the Akka system.
     * @return Configured Akka actor system.
     */
    @Bean
    public akka.actor.typed.ActorSystem<SourceRootActor.Command> actorTypedSystem(
            @Value("${app.search.actors.max:100}") int maxAmountOfSearchActors,
            @Value("${app.cluster.hostname:'localhost'}") String hostName,
            @Value("${app.cluster.port:2551}") int port,
            @Value("${app.cluster.nodes:'localhost:2551'}") String nodes,
            @Value("${spring.application.name}") String applicationName) {
        AkkaClusterProvider clusterProvider = new LocalClusterProvider();

        ClusterConfigurationProperties clusterConfigurationProperties
                = ClusterConfigurationProperties.builder()
                .systemName(applicationName)
                .maxSearchActors(maxAmountOfSearchActors)
                .clusterHostName(hostName)
                .clusterPort(port)
                .seedNodes(Arrays.asList(nodes.split(","))).build();
        return clusterProvider.actorTypedSystem(clusterConfigurationProperties);
    }

    /**
     * Creates a task actor using the Akka actor system.
     *
     * @param system The Akka actor system.
     * @return ActorRef for the TaskServiceActor.
     */
    @Bean
    public ActorRef<TaskServiceActor.Command> taskActor(akka.actor.typed.ActorSystem<SourceRootActor.Command> system) {
        return AskPattern.ask(system, (ActorRef<SourceRootActor.StartedActor<TaskServiceActor.Command>> replyTo) ->
                        new SourceRootActor.StartActor<>(TaskServiceActor.create(), replyTo),
                Duration.ofMinutes(1),
                system.scheduler()).toCompletableFuture().join().actorRef;
    }

    /**
     * Retrieves the classic Akka ActorSystem from the typed actor system.
     *
     * @param system The typed Akka actor system.
     * @return Classic ActorSystem instance.
     */
    @Bean
    public ActorSystem actorSystem(akka.actor.typed.ActorSystem<SourceRootActor.Command> system) {
        return system.classicSystem();
    }
}
