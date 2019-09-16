
dubflight: bin
	gcc -o bin/dubflight src/*.c -lm -Wall -Wextra -g

bin:
	mkdir -p bin

run: dubflight
	./bin/dubflight

lint:
	infer run -- make
	cppcheck --enable=all src/*
	valgrind bin/dubflight

clean:
	rm -rf bin infer-out
