FROM python:3.8

RUN pip3 install platformio

WORKDIR /opt/paradrone

# COPY Makefile Makefile
# COPY src src

# RUN make
