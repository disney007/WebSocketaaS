FROM nginx:latest
ENV TARGET_DIR "/opt/data/"
RUN mkdir -p $TARGET_DIR
COPY "scripts/*" $TARGET_DIR
COPY "nginx.conf" "/etc/nginx/"
