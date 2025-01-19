package org.poo.main.exchange_rate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.AbstractMap;

/**
 * Represents the exchange rate between two currencies.
 */
public final class ExchangeRate {
    private final String from;
    private final String to;
    private final double rate;

    public ExchangeRate(final String from, final String to, final double rate) {
        this.from = from;
        this.to = to;
        this.rate = rate;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getRate() {
        return rate;
    }

    /**
     * Finds the exchange rate between two currencies using BFS.
     */
    public static double getExchangeRate(final String from, final String to,
                                         final List<ExchangeRate> exchangeRates) {
        // Build the graph as an adjacency map
        Map<String, Map<String, Double>> graph = buildGraph(exchangeRates);

        if (!graph.containsKey(from) || !graph.containsKey(to)) {
            return 0;
        }

        // Set to track visited nodes
        Set<String> visited = new HashSet<>();

        // Search using BFS
        return bfs(graph, from, to, visited);
    }

    /**
     * Builds the graph from the list of ExchangeRate objects.
     */
    private static Map<String, Map<String, Double>> buildGraph(final List<ExchangeRate>
                                                                       exchangeRates) {
        Map<String, Map<String, Double>> graph = new HashMap<>();
        for (ExchangeRate rate : exchangeRates) {
            graph.putIfAbsent(rate.getFrom(), new HashMap<>());
            graph.putIfAbsent(rate.getTo(), new HashMap<>());

            graph.get(rate.getFrom()).put(rate.getTo(), rate.getRate());
            graph.get(rate.getTo()).put(rate.getFrom(), 1.0 / rate.getRate());
        }
        return graph;
    }

    /**
     * BFS algorithm for calculating the exchange rate.
     */
    private static double bfs(final Map<String, Map<String, Double>> graph, final String start,
                              final String target, final Set<String> visited) {
        // Using a queue
        Queue<Map.Entry<String, Double>> queue = new LinkedList<>();
        queue.offer(new AbstractMap.SimpleEntry<>(start, 1.0));

        // Add the start node to the visited set
        visited.add(start);

        // Explore the queue
        while (!queue.isEmpty()) {
            Map.Entry<String, Double> current = queue.poll();
            String currentCurrency = current.getKey();
            double currentProduct = current.getValue();

            if (currentCurrency.equals(target)) {
                return currentProduct;
            }

            // Explore the neighbors
            for (Map.Entry<String, Double> neighbor : graph.get(currentCurrency).entrySet()) {
                String nextCurrency = neighbor.getKey();
                double rate = neighbor.getValue();

                if (!visited.contains(nextCurrency)) {
                    visited.add(nextCurrency);
                    // Add the neighbor to the queue
                    queue.offer(new AbstractMap.SimpleEntry<>(nextCurrency,
                            currentProduct * rate));
                }
            }
        }

        return 0;
    }

    /**
     * Converts an amount to RON based on exchange rates.
     */
    public static double convertToRON(final double amount, final String currency,
                                      final List<ExchangeRate> exchangeRates) {
        if ("RON".equals(currency)) {
            return amount; // Already in RON
        }

        double rate = getExchangeRate(currency, "RON", exchangeRates);
        if (rate > 0) {
            return amount * rate;
        }

        return amount; // Return original amount if no conversion is possible
    }

    /**
     * Converts an amount from RON to the specified currency.
     */
    public static double convertFromRON(final double amount, final String currency,
                                        final List<ExchangeRate> exchangeRates) {
        if ("RON".equals(currency)) {
            return amount; // Already in RON
        }

        double rate = getExchangeRate("RON", currency, exchangeRates);
        if (rate > 0) {
            return amount / rate;
        }

        return amount; // Return original amount if no conversion is possible
    }
}
