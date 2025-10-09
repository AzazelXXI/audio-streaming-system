app:
	clear
	@mvn clean compile exec:java -Dexec.mainClass="com.audio.audiostreaming.AudioStreaming"
server:
	clear
	@mvn clean compile exec:java -Dexec.mainClass="serverside.Server"