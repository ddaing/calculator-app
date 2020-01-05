#include <jni.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>

#define TEXTLCD_BASE		0x56
#define TEXTLCD_FUNCTION_SET    	_IO(TEXTLCD_BASE,0x31)

#define TEXTLCD_DISPLAY_ON 			_IO(TEXTLCD_BASE,0x32)
#define TEXTLCD_DISPLAY_OFF 		_IO(TEXTLCD_BASE,0x33)
#define TEXTLCD_DISPLAY_CURSOR_ON 	_IO(TEXTLCD_BASE,0x34)
#define TEXTLCD_DISPLAY_CURSOR_OFF 	_IO(TEXTLCD_BASE,0x35)

#define TEXTLCD_CURSOR_SHIFT_RIGHT  _IO(TEXTLCD_BASE,0x36)
#define TEXTLCD_CURSOR_SHIFT_LEFT   _IO(TEXTLCD_BASE,0x37)

#define TEXTLCD_ENTRY_MODE_SET  	_IO(TEXTLCD_BASE,0x38)
#define TEXTLCD_RETURN_HOME     	_IO(TEXTLCD_BASE,0x39)
#define TEXTLCD_CLEAR           	_IO(TEXTLCD_BASE,0x3a)

#define TEXTLCD_DD_ADDRESS_1		_IO(TEXTLCD_BASE,0x3b)
#define TEXTLCD_DD_ADDRESS_2		_IO(TEXTLCD_BASE,0x3c)
#define TEXTLCD_WRITE_BYTE      	_IO(TEXTLCD_BASE,0x3d)

#define GPIO_OUTPUT 0
#define GPIO_INPUT	1
#define GPIO_LOW	0
#define GPIO_HIGH	1

#define GPIO_TEXT_EN	104
#define GPIO_TEXT_RW	105
#define GPIO_TEXT_RS	106

#define GPIO_TEXT_D0	150
#define GPIO_TEXT_D1	151
#define GPIO_TEXT_D2	152
#define GPIO_TEXT_D3	153
#define GPIO_TEXT_D4	154
#define GPIO_TEXT_D5	155
#define GPIO_TEXT_D6	156
#define GPIO_TEXT_D7	157

#define SYSFS_GPIO_DIR "/sys/class/gpio"

#define MAX_BUF 128

unsigned int textlcd_control[3]={GPIO_TEXT_EN,GPIO_TEXT_RW,GPIO_TEXT_RS};
unsigned int textlcd_data[8]={GPIO_TEXT_D0,GPIO_TEXT_D1,GPIO_TEXT_D2,GPIO_TEXT_D3,GPIO_TEXT_D4,GPIO_TEXT_D5,GPIO_TEXT_D6,GPIO_TEXT_D7};

JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_gpioexport(JNIEnv * env, jobject obj, jint gpio){
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
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_gpiounexport(JNIEnv * env, jobject obj, jint gpio){

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
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_gpiosetdir(JNIEnv * env, jobject obj,jint gpio,jint dir, jint val){
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

void setcommand(unsigned char command){
	int i;

	gpio_set_val(textlcd_control[2], GPIO_LOW);
	gpio_set_val(textlcd_control[0], GPIO_LOW);

	usleep(10);
	for(i=0; i<8; i++)
	{
		if (command & 0x01)
			gpio_set_val(textlcd_data[i],GPIO_HIGH);
		else
			gpio_set_val(textlcd_data[i],GPIO_LOW);

		command >>= 1;
	}
	usleep(10);

	gpio_set_val(textlcd_control[0],GPIO_HIGH);
	usleep(10);
	gpio_set_val(textlcd_control[0],GPIO_LOW);
	usleep(41);
}
void writebyte(unsigned char cData){
	int i;
	gpio_set_val(textlcd_control[2], GPIO_HIGH);
	gpio_set_val(textlcd_control[0], GPIO_LOW);
	usleep(10);
	for(i=0; i<8; i++)	{
		if (cData & 0x01)
			gpio_set_val(textlcd_data[i],GPIO_HIGH);
		else
			gpio_set_val(textlcd_data[i],GPIO_LOW);

		cData >>= 1;
	}
	usleep(10);

	gpio_set_val(textlcd_control[0],GPIO_HIGH);
	usleep(10);
	gpio_set_val(textlcd_control[0],GPIO_LOW);
	usleep(41);
}

JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_initializetextlcd(JNIEnv * env, jobject obj){
	setcommand(0x38);
	setcommand(0x38);
	setcommand(0x38);
	setcommand(0x0c);
	setcommand(0x01);
	usleep(1960);
	setcommand(0x06);
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_DisplayControl(JNIEnv * env, jobject obj,jint display_enable){
	if(display_enable == 0)
		setcommand(0x0c);
	else
		setcommand(0x08);

	return 1;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_CursorControl(JNIEnv * env, jobject obj,jint cursor_enable){
	if(cursor_enable == 0)
		setcommand(0x0e);
	else
		setcommand(0x0c);

	return 1;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_CursorShift(JNIEnv * env, jobject obj,jint set_shift){
	if (set_shift == 0)
		setcommand(0x14);
	else
		setcommand(0x10);

	return 1;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_Cursorhome(JNIEnv * env, jobject obj){
	setcommand(0x02);
	return 1;
}

JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_DisplayClear(JNIEnv * env, jobject obj){
	setcommand(0x01);
	return 1;
}
int count(char cNum[])
{
	int i=0;
	while(cNum[i] != '\0') i++;
	return i;
}
int set_ddram_address(int pos)
{
	if (pos == 0)
		setcommand(0x80);
	else
		setcommand(0xC0);

	return 1;
}
JNIEXPORT jint JNICALL Java_kr_ac_ajou_esd_calculator_jni_TextLCDDriver_DisplayWrite(JNIEnv * env, jobject obj,jint line, jstring data,jint len){
	int  iSize = 0, i;
	jboolean iscopy;
	char *str_utf = (*env)->GetStringUTFChars(env, data, &iscopy);

	if (line == 1)	{
		set_ddram_address(0);
	}else if (line == 2)	{
		set_ddram_address(1);
	}
	for(i=0; i<len; i++)
		writebyte(str_utf[i]);

	(*env)->ReleaseStringUTFChars(env, data, str_utf);
}
