reg:
	javac *.java
	rmic MarketPlace
	rmic PaymentEngine
	rmiregistry

market: Marketplace.java MarketplaceInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy Marketplace

client: Client.java PaymentEngine.java PaymentEngineInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy Client

clean:
	rm *.class
