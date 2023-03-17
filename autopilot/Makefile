
paradrone: bin
	g++ \
		-I .pio/libdeps/native/Unity/src .pio/libdeps/native/Unity/src/unity.c \
		-I lib/paralib lib/paralib/*.cpp test/test.cpp \
		-lm -Wall -Wextra -g -DPIO_UNIT_TESTING -o bin/paradrone

bin:
	mkdir -p bin

run: paradrone
	./bin/paradrone

lint:
	infer run -- make
	cppcheck --enable=all src/*
	valgrind bin/paradrone

docs:
	@doxygen

clean:
	rm -rf bin html infer-out */*.gch */*/*.gch
