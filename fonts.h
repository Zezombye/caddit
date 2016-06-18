#ifndef FONTS_H
#define FONTS_H

struct Font {
	unsigned long ltr[130][2]; //an array of longs for each character
	char length[130]; //array of chars for the length of each character
};

extern struct Font normfont;

int dispStr(unsigned char* str, struct Font font, int x, int y, int strlen);

#endif