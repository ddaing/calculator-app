#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>
#include <termios.h>
#include <errno.h>

static struct termios initial_settings, new_settings;
static int peek_character = -1;

#define GPIO_OUTPUT 0
#define GPIO_INPUT	1
#define GPIO_LOW	0
#define GPIO_HIGH	1

#define GPIO_SEG_SEL0	112
#define GPIO_SEG_SEL1	113
#define GPIO_SEG_SEL2	114
#define GPIO_SEG_SEL3	115
#define GPIO_SEG_SEL4	116
#define GPIO_SEG_SEL5	108

#define GPIO_SEG_DATA_A	192
#define GPIO_SEG_DATA_B	193
#define GPIO_SEG_DATA_C	194
#define GPIO_SEG_DATA_D	195
#define GPIO_SEG_DATA_E	196
#define GPIO_SEG_DATA_F	197
#define GPIO_SEG_DATA_G	198
#define GPIO_SEG_DATA_H	199

#define SYSFS_GPIO_DIR "/sys/class/gpio"

#define MAX_BUF 128

#define ERROR 111111
#define SHOWINT 111112
#define SHOWDOUBLE 111113
unsigned int seg_sel[6]={GPIO_SEG_SEL0,GPIO_SEG_SEL1,GPIO_SEG_SEL2,GPIO_SEG_SEL3,GPIO_SEG_SEL4,GPIO_SEG_SEL5};
unsigned int seg_data[8]={GPIO_SEG_DATA_A,GPIO_SEG_DATA_B,GPIO_SEG_DATA_C,GPIO_SEG_DATA_D,GPIO_SEG_DATA_E,GPIO_SEG_DATA_F,GPIO_SEG_DATA_G,GPIO_SEG_DATA_H};
unsigned int ibuf[8];
int mCount = 0;
int stop_flag =1;
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SegmentDriver_gpioexport(JNIEnv * env, jobject obj, jint gpio){
	int fd, len;
	char buf[MAX_BUF];

	fd = open(SYSFS_GPIO_DIR "/export", O_WRONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't export GPIO %d pin: %s\n", gpio, strerror(errno));
		return fd;
	}

	len = snprintf(buf, sizeof(buf), "%d", gpio);
	write(fd, buf, len);
	close(fd);

	return 0;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SegmentDriver_gpiosetdir(JNIEnv * env, jobject obj,jint gpio,jint dir, jint val){
	int fd, len;
	char buf[MAX_BUF];

	len = snprintf(buf, sizeof(buf), SYSFS_GPIO_DIR  "/gpio%d/direction", gpio);

	fd = open(buf, O_WRONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't set GPIO %d pin direction: %s\n", gpio, strerror(errno));
		return fd;
	}

	if (dir == GPIO_OUTPUT) {
		if (val == GPIO_HIGH)
			write(fd, "high", 5);
		else
			write(fd, "out", 4);
	} else {
		write(fd, "in", 3);
	}

	close(fd);

	return 0;
}
int gpio_set_val(int gpio, int val){

	int fd, len;
	char buf[MAX_BUF];

	len = snprintf(buf, sizeof(buf), SYSFS_GPIO_DIR "/gpio%d/value", gpio);

	fd = open(buf, O_WRONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't set GPIO %d pin value: %s\n", gpio, strerror(errno));
		return fd;
	}

	if (val == GPIO_HIGH)
		write(fd, "1", 2);
	else
		write(fd, "0", 2);

	close(fd);
	return 0;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SegmentDriver_gpiounexport(JNIEnv * env, jobject obj, jint gpio){
	int fd, len;
	char buf[MAX_BUF];

	fd = open(SYSFS_GPIO_DIR "/unexport", O_WRONLY);

	if (fd < 0) {
		fprintf(stderr, "Can't unexport GPIO %d pin: %s\n", gpio, strerror(errno));
		return fd;
	}

	stop_flag = 0;
	len = snprintf(buf, sizeof(buf), "%d", gpio);
	write(fd, buf, len);
	close(fd);

	return 0;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SegmentDriver_StopSegment(JNIEnv * env, jobject obj, jint flag){
	stop_flag = flag;
	return 0;
}
int Getsegmentcode_base (int x)
{
  unsigned int i;

  for (i = 0; i < 8; i++)
    ibuf[i] = 0;

  switch (x) {
    case 0 :
      for (i=0; i<6; i++) ibuf[i] = 1;
      break;

    case 1 : ibuf[1] = 1; ibuf[2] = 1; break;

    case 2 :
      for (i=0; i<2; i++) ibuf[i] = 1;
      for (i=3; i<5; i++) ibuf[i] = 1;
      ibuf[6] = 1;
      break;

    case 3 :
      for (i=0; i<4; i++) ibuf[i] = 1;
      ibuf[6] = 1;
      break;

    case 4 :
      for (i=1; i<3; i++) ibuf[i] = 1;
      for (i=5; i<7; i++) ibuf[i] = 1;
      break;

    case 5 :
      ibuf[0] = 1;
      for (i=2; i<4; i++) ibuf[i] = 1;
      for (i=5; i<7; i++) ibuf[i] = 1;
      break;

    case 6 :
      for (i=2; i<7; i++) ibuf[i] = 1;
      break;

    case 7 :
      for (i=0; i<3; i++) ibuf[i] = 1;
      ibuf[5] = 1;
      break;

    case 8 :
      for (i=0; i<7; i++) ibuf[i] = 1;
      break;

    case 9 :
      for (i=0; i<4; i++) ibuf[i] = 1;
      for (i=5; i<7; i++) ibuf[i] = 1;
      break;

    case 10 :
      for (i=0; i<3; i++) ibuf[i] = 1;
      for (i=4; i<8; i++) ibuf[i] = 1;
      break;
    case 11 :
      for (i=0; i<8; i++) ibuf[i] = 1;
      break;
    case 12 :
      for (i=3; i<6; i++) ibuf[i] = 1;
      ibuf[0] = 1;
      ibuf[7] = 1;
      break;
    case 13 :
      ibuf[7] = 1;
      for (i=0; i<6; i++) ibuf[i] = 1;
      break;
    case 14 :
      for (i=3; i<7; i++) ibuf[i] = 1;
      ibuf[0] = 1;
      break;
    case 15 :
      for (i=4; i<7; i++) ibuf[i] = 1;
      ibuf[0] = 1;
      break;
    case 16 : //r
      ibuf[4] = 1;
      ibuf[6] = 1;
      break;
    case 17 : // o
      for(i=2; i<5; i++) ibuf[i] = 1;
      ibuf[6] = 1;
      break;
    case 18 : // U
      for(i=1; i<6; i++) ibuf[i] = 1;
      break;
    case 19 : // L
      for(i=3; i<6; i++) ibuf[i] = 1;

      break;
    case 20 : // N
      for(i=0; i<3; i++) ibuf[i] = 1;
      for(i=4; i<6; i++) ibuf[i] = 1;
      break;
    case 21 : // T
      for(i=0; i<3; i++) ibuf[i] = 1;
      break;
    default :
      for (i=0; i<8; i++) ibuf[i] = 1;
      break;
  }
  return 0;
}

JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_SegmentDriver_segcontrol(JNIEnv * env, jobject obj, jint val){
	//0 to 6
	int i,j;
	int isRealMode;
	if(val == ERROR){
		for ( i=0; i < 4; i++){
			Getsegmentcode_base(14);

			for(j=0;j<8;j++)
				gpio_set_val(seg_data[j], ibuf[j]);

			gpio_set_val(seg_sel[0], GPIO_LOW);
			usleep(100000);
			Getsegmentcode_base(16);
			gpio_set_val(seg_sel[0], GPIO_HIGH);

			for(j=0;j<8;j++)
				gpio_set_val(seg_data[j], ibuf[j]);

			gpio_set_val(seg_sel[1], GPIO_LOW);
			usleep(100000);
			Getsegmentcode_base(16);
			gpio_set_val(seg_sel[1], GPIO_HIGH);
			for(j=0;j<8;j++)
				gpio_set_val(seg_data[j], ibuf[j]);

			gpio_set_val(seg_sel[2], GPIO_LOW);
			usleep(100000);
			Getsegmentcode_base(17);
			gpio_set_val(seg_sel[2], GPIO_HIGH);
			for(j=0;j<8;j++)
				gpio_set_val(seg_data[j], ibuf[j]);

			gpio_set_val(seg_sel[3], GPIO_LOW);
			usleep(100000);
			Getsegmentcode_base(16);
			gpio_set_val(seg_sel[3], GPIO_HIGH);
			for(j=0;j<8;j++)
				gpio_set_val(seg_data[j], ibuf[j]);

			gpio_set_val(seg_sel[4], GPIO_LOW);
			usleep(100000);

			gpio_set_val(seg_sel[4], GPIO_HIGH);
		}
	}else if(val == SHOWINT){
		Getsegmentcode_base(1);

		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[0], GPIO_LOW);
			usleep(100000);
		Getsegmentcode_base(20);
		gpio_set_val(seg_sel[0], GPIO_HIGH);

		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[1], GPIO_LOW);
		usleep(100000);
		Getsegmentcode_base(21);
		gpio_set_val(seg_sel[1], GPIO_HIGH);
		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[2], GPIO_LOW);
		usleep(100000);

		gpio_set_val(seg_sel[2], GPIO_HIGH);


					gpio_set_val(seg_sel[5], GPIO_HIGH);
	}else if(val == SHOWDOUBLE){
		Getsegmentcode_base(13);

		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[0], GPIO_LOW);
			usleep(100000);
		Getsegmentcode_base(0);
		gpio_set_val(seg_sel[0], GPIO_HIGH);

		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[1], GPIO_LOW);
		usleep(100000);
		Getsegmentcode_base(18);
		gpio_set_val(seg_sel[1], GPIO_HIGH);
		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[2], GPIO_LOW);
		usleep(100000);
		Getsegmentcode_base(11);
		gpio_set_val(seg_sel[2], GPIO_HIGH);
		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[3], GPIO_LOW);
		usleep(100000);
		Getsegmentcode_base(19);
		gpio_set_val(seg_sel[3], GPIO_HIGH);
		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);

		gpio_set_val(seg_sel[4], GPIO_LOW);
		usleep(100000);
		Getsegmentcode_base(14);
		gpio_set_val(seg_sel[4], GPIO_HIGH);
		for(j=0;j<8;j++)
			gpio_set_val(seg_data[j], ibuf[j]);
		gpio_set_val(seg_sel[5], GPIO_LOW);
		usleep(100000);

		gpio_set_val(seg_sel[5], GPIO_HIGH);
	}
	return 0;
}
