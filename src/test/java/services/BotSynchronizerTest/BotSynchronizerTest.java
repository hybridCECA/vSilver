package services.BotSynchronizerTest;

import coinsources.CoinSources;
import database.Connection;
import dataclasses.*;
import nicehash.NHApi;
import nicehash.OrderBot;
import org.junit.AfterClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import services.AdjustBot;
import services.BotSynchronizer;
import services.MarketEvaluator;
import utils.SingletonFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class BotSynchronizerTest {
    private static final Answer<Object> DO_NOTHING = new Answer<Object>() {
        @Override
        public Object answer(InvocationOnMock invocationOnMock) {
            return null;
        }
    };

    @Test
    public void testBotSynchronizer() {
        Set<OrderBot> orderBots = new HashSet<>();
        Set<OrderBot> apiOrderBots = new HashSet<>();
        Map<String, Double> limitMap = new HashMap<>();

        MockNHApi mockNHApi = new MockNHApi();
        SingletonFactory.setInstance(NHApi.class, mockNHApi);
        mockNHApi.setActiveOrders(apiOrderBots);

        MockCoinSources mockCoinSources = new MockCoinSources();
        SingletonFactory.setInstance(CoinSources.class, mockCoinSources);

        try (
                MockedStatic<AdjustBot> mockedAdjustBot = mockStatic(AdjustBot.class);
                MockedStatic<Connection> mockedConnection = mockStatic(Connection.class);
                MockedStatic<MarketEvaluator> mockedMarketEvaluator = mockStatic(MarketEvaluator.class)
        ) {
            mockedAdjustBot.when(AdjustBot::getOrderBots).thenReturn(orderBots);

            mockedConnection.when(Connection::getOrderInitialLimits).thenReturn(limitMap);
            mockedConnection.when(() -> Connection.deleteOrderInitialData(anyString())).thenAnswer(DO_NOTHING);
            mockedConnection.when(() -> Connection.putOrderInitialData(anyString(), anyDouble(), anyDouble())).thenAnswer(DO_NOTHING);

            mockedMarketEvaluator.when(() -> MarketEvaluator.hasProfitReport(any(CryptoInvestment.class))).thenReturn(true);
            ProfitReport blankReport = new ProfitReport(0, 0,0,0,0, 0);
            mockedMarketEvaluator.when(() -> MarketEvaluator.getProfitReport(any(CryptoInvestment.class))).thenReturn(blankReport);

            // Test basic add and removal
            OrderBot botToAdd = new OrderBot("bot1", 0, "", "", "");
            apiOrderBots.add(botToAdd);
            OrderBot botToRemove = new OrderBot("bot2", 0, "", "", "");
            orderBots.add(botToRemove);
            BotSynchronizer synchronizer = new BotSynchronizer();
            synchronizer.run();
            assertTrue(orderBots.contains(botToAdd));
            assertFalse(orderBots.contains(botToRemove));

            // Test algo filter
            mockCoinSources.setValidCoin(false);
            synchronizer.run();
            assertFalse(orderBots.contains(botToAdd));

            // Test profit reports not ready
            mockedMarketEvaluator.when(() -> MarketEvaluator.hasProfitReport(any(CryptoInvestment.class))).thenReturn(false);
            OrderBot botToAdd2 = new OrderBot("bot3", 0, "", "", "");
            apiOrderBots.add(botToAdd2);
            synchronizer.run();
            assertFalse(orderBots.contains(botToAdd2));

            // Test database clean
            final String BOT4_ID = "bot4";
            limitMap.put(BOT4_ID, 0D);
            AtomicBoolean pass = new AtomicBoolean(false);
            Answer<Object> passTest = new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) {
                    pass.set(true);
                    return null;
                }
            };
            mockedConnection.when(() -> Connection.deleteOrderInitialData(BOT4_ID)).thenAnswer(passTest);
            synchronizer.run();
            assertTrue(pass.get());
        }
    }

    @AfterClass
    public static void teardown() {
        SingletonFactory.clearInstances();
    }
}