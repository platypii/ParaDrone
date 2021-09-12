
paradrone: bin
	g++ \
		-I ~/.platformio/packages/tool-unity ~/.platformio/packages/tool-unity/unity.c \
		-I lib/paralib lib/paralib/*.cpp test/test.cpp \
		-lm -Wall -Wextra -g -DUNIT_TEST -o bin/paradrone

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
