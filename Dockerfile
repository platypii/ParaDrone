FROM alpine:3.11

RUN apk add make gcc libc-dev

WORKDIR /opt/paradrone

# COPY Makefile Makefile
# COPY src src

# RUN make
