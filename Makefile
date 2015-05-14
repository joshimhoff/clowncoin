reg:
	javac *.java
	rmic Marketplace
	rmic PaymentEngine
	rmiregistry

market: Marketplace.java MarketplaceInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy -Djava.rmi.server.hostname=139.140.192.154 Marketplace

client: Client.java PaymentEngine.java PaymentEngineInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy -Djava.rmi.server.hostname=139.140.192.154 Client

clean:
	rm *.class
