.PHONY: clean rebuild

BurpSessions.jar:
	./gradlew build

clean:
	./gradlew clean

rebuild: clean
	./gradlew build
