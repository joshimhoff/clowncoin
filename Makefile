reg:
	javac *.java
	rmic PaymentEngine
	rmiregistry

client: Client.java PaymentEngine.java Receiver.java
	javac *.java
	java -Djava.security.policy=ClownCoin.policy Client

clean:
	rm *.class