FROM openjdk:8

WORKDIR /app/bin

ENV DATA_DIR /data

EXPOSE 8080

# Copy the application from its folder to ours
COPY ./build/install/monolyth-server/ /app

# Run the app when the container is executed.
ENTRYPOINT ["./monolyth-server"]
