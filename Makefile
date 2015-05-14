reg:
	javac *.java
	rmic Marketplace
	rmic PaymentEngine
	rmiregistry

all: Marketplace.java MarketplaceInterface.java Client.java Verifier.java PaymentEngine.java PaymentEngineInterface.java
	javac *.java
	#java -Djava.security.policy=ClownCoin.policy -Djava.rmi.server.hostname=139.140.192.154 Marketplace
	#java -Djava.security.policy=ClownCoin.policy -Djava.rmi.server.hostname=139.140.192.103 Client

clean:
	rm *.class
