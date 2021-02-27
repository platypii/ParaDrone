
paradrone: bin
	g++ lib/paralib/*.cpp lib/paralib/*.h -lm -Wall -Wextra -g

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
	rm -rf bin html infer-out
