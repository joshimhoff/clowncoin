reg:
	javac *.java
	rmic KeyServer
	rmic PaymentEngine
	rmiregistry

key: KeyServer.java KeyServerInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy KeyServer

client: Client.java PaymentEngine.java PaymentEngineInterface.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy Client

clean:
	rm *.class
