reg:
	javac *.java
	rmic Marketplace
	rmic PaymentEngine
	rmiregistry

coin: Marketplace.java MarketplaceInterface.java Client.java Verifier.java PaymentEngine.java PaymentEngineInterface.java
	javac *.java

clean:
	rm *.class
