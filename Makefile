
dubflight: bin
	gcc -o bin/dubflight src/*.c -lm -Wall -Wextra -g

bin:
	mkdir -p bin

run: dubflight
	./bin/dubflight

clean:
	rm -rf bin
