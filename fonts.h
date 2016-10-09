#ifndef FONTS_H
#define FONTS_H

struct Font {
	unsigned long ltr[220][2]; //an array of longs for each character
	char length[220]; //array of chars for the length of each character
};

extern struct Font normfont;

int dispStr(unsigned char* str, int x, int y);

#endif