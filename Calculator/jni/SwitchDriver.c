#include <jni.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>

#define GPIO_OUTPUT 0
#define GPIO_INPUT	1
#define GPIO_LOW	0
#define GPIO_HIGH	1

#define SYSFS_GPIO_DIR "/sys/class/gpio"

#define MAX_BUF 128
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SwitchDriver_gpioexport(JNIEnv * env, jobject obj, jint gpio){
	int fd, len;
	char buf[MAX_BUF];

	fd = open(SYSFS_GPIO_DIR "/export", O_WRONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't export GPIO %d pin: %s\n", gpio, strerror(errno));
		return -1;
	}

	len = snprintf(buf, sizeof(buf), "%d", gpio);
	write(fd, buf, len);
	close(fd);

	return 0;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SwitchDriver_gpiounexport(JNIEnv * env, jobject obj, jint gpio){

		int fd, len;
		char buf[MAX_BUF];

		fd = open(SYSFS_GPIO_DIR "/unexport", O_WRONLY);

		if (fd < 0) {
			fprintf(stderr, "Can't unexport GPIO %d pin: %s\n", gpio, strerror(errno));
			return -1;
		}

		len = snprintf(buf, sizeof(buf), "%d", gpio);
		write(fd, buf, len);
		close(fd);
	return 0;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SwitchDriver_gpiogetval(JNIEnv * env, jobject obj, jint gpio){

	int fd, len,val=0;
	char buf[MAX_BUF];

	len = snprintf(buf, sizeof(buf), SYSFS_GPIO_DIR "/gpio%d/value", gpio);

	fd = open(buf, O_RDONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't get GPIO %d pin value: %s\n", gpio, strerror(errno));
		return fd;
	}

	read(fd, buf, 1);
	close(fd);

	if (*buf != '0')
		val = GPIO_HIGH;
	else
		val = GPIO_LOW;

	return val;
}
