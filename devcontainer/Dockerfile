FROM thyrlian/android-sdk-vnc:5.0

WORKDIR /opt
RUN wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/4.1.0.19/android-studio-ide-201.6858069-linux.tar.gz
RUN tar -xvzf android-studio-ide-201.6858069-linux.tar.gz
RUN rm -f android-studio-ide-201.6858069-linux.tar.gz
COPY android-studio.desktop /root/.local/share/applications/android-studio.desktop

ENV PATH="/opt/android-studio/bin:/opt/android-sdk/cmdline-tools/tools/bin/:${PATH}"

RUN mkdir /sdk && chmod 666 /sdk
RUN mkdir /workspace && chmod 666 /workspace

WORKDIR /workspace