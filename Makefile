
build:
	mkdir -p bin
	gcc -o bin/dubflight src/main.c -lm

run:
	./bin/dubflight

clean:
	rm -rf bin
