#include "fxlib.h"
#include <stdio.h>
#include <stdlib.h>
#include "MonochromeLib.h"
#include "fonts.h"
#include "serial.h"

#define SCROLL 8 //number of pixels to scroll at each press of the up/down key

unsigned long posY[2] = {0}; //array holding the scroll for each view, so that if the user comes back from a comment page, he keeps his scroll position on the post page
unsigned int key; 
unsigned int postHeights[26] = {0};
//char postComments[26];
char currentView = 0; //0=post, 1=comment

unsigned char strReceived[5010];

unsigned char byteReceived;
unsigned char byteSent;
char stringFinished = 1;


//const unsigned char str[] = "<r /r/SandersForPresident> <p t=Here are some dank memes:\nLook of disapproval: \x82\_\x82\nLenny face: (\x83\x84\x83\)\nSunglasses: (\x86\x85\_\x85\)\nShrug: \x87\\\_(\x88\)_/\x87\" a=(ext.link) r/sandersforpresident u/Zezombye 45 upvotes\"> <p t=This is the title of the second post! Gotta make it long though so that I have to scroll.\" a=(self) r/askreddit u/lephenixnoir\"> <p t=the quick brown fox jumps over the lazy dog\nTHE QUICK BROWN FOX JUMPS OVER THE LAZY DOG!!! (why are we yelling?)\" a=goddammit test\"> <p t=Fourth post\" a=.\"";

//postComments[0] = strCmt;
//postComments[1] = strCmt2;
//postComments[2] = strCmt3;

void dispPost(unsigned char* str, int strlen);
void getPost(unsigned long posY);
void dispCmt(unsigned char* str, int strlen);
void sendSerial(unsigned char* code);
void getSerial();

int AddIn_main(int isAppli, unsigned short OptionNum)

{
	unsigned char serialSettings[]={0,5,0,0,0,0};
	unsigned char test[] = "TestTransmission\n";
	Serial_Open(serialSettings);
	ML_clear_vram();
	//GetKey(&key);
	sendSerial("sc");
	dispStr(test, normfont, 1, 1, sizeof(test));
	
	getSerial();
    while(1) {
		ML_clear_vram();
		
		//displays the current view
		if (currentView == 0)
			dispPost(strReceived, sizeof(strReceived));
		else
			dispCmt(strReceived, sizeof(strReceived));
			//dispCmt(strCmt, sizeof(strCmt)); //put this line once demo is finished
			//getPost(posY[0]);
		
		ML_pixel(127,31,1); //debug pixel, used to test the "hitbox" of posts
        GetKey(&key);
		if (key == KEY_CTRL_DOWN || key == KEY_CTRL_UP) {
			if (key == KEY_CTRL_DOWN)
				posY[currentView] += SCROLL;
			else if (posY[currentView] >= SCROLL)
				posY[currentView] -= SCROLL;
		}
		if (key == KEY_CTRL_EXE && currentView == 0) {
			getPost(posY[0]);
			posY[1] = 0;
		}
		if (key == KEY_CTRL_EXIT && currentView == 1) {
			currentView = 0;
		}
		if (key == KEY_CTRL_SHIFT && !stringFinished) {
			sendSerial("np");
			getSerial();
		}
    }

    return 1;
}

void sendSerial(unsigned char* code) {
	//Serial_WriteByte('&');
	Serial_WriteByte(code[0]);
	Serial_WriteByte(code[1]);
	Serial_WriteByte(';');
	Serial_WriteByte('\n');
}

void getSerial() {
	int i = 0;
	memset(strReceived, 0, sizeof(strReceived));
	Serial_ClearReceiveBuffer();
	byteReceived = '\0';
	//locate(1,1); Print((unsigned char*)"Testttt");
	while (1) {	
		while(Serial_ReadByte(&byteReceived)==0 && byteReceived != '"' && byteReceived != '\0') {
			strReceived[i] = byteReceived;
			i++;
		}
		if (byteReceived == '"') {
			if (strReceived[i-1] == ',') {
				sendSerial("ak");
				i--;
				strReceived[i] = '\0';
				byteReceived = '\0';
				Serial_ClearReceiveBuffer();
			} else {
				if (strReceived[i-1] == ';')
					stringFinished = 0;
				else
					stringFinished = 1;
				break;
			}
		}
	}
	//dispStr(strReceived, normfont, 10, 10, i);
}

void getPost(unsigned long posY) {
	int height = 12;
	int rankOfPost = 0;
	while (1) {
		height += postHeights[rankOfPost]+3;
		
		if (height > posY+33) {
			unsigned char str[3] = "p ";
			str[1] = rankOfPost+65;
			ML_clear_vram();
			currentView = 1;
			
			sendSerial(str);
			getSerial();
			break;
		}
		if (rankOfPost > 25)
			break;
			
		rankOfPost++;
	}
}

void dispCmt(unsigned char* str, int strlen) {
	int i;
	int currentPageHeight = 0;
	
	for (i = 0; i < strlen; i++) {
		if (str[i] == '<') {
			if (str[i+1] == 't') { //it is the post
				int j = i+3;
				int k = 0;
				unsigned char post[30100] = {0};
				
				while (str[j] != '>') { //stocks the text of the post
					post[k] = str[j];
					k++;
					j++;
				}
				
				currentPageHeight += dispStr(post, normfont, 0, currentPageHeight-posY[1], k); //increases the page height and displays it
				
				//ML_horizontal_line(currentPageHeight-posY[1], 0, 127, 1); //draws a line after the post
				
				//currentPageHeight += 1;
				i = j;
			}
			if (str[i+1] >= '1' && str[i+1] <= '9') { //it is a comment
				int j = i+3;
				int k = 0;
				unsigned char comment[2000] = {0};
				int heightOfComment = 0;
				while (str[j] != '>') {
					comment[k] = str[j];
					k++;
					j++;
				}
				
				if (str[i+1] == '1') {
					ML_horizontal_line(currentPageHeight-posY[1], 0, 127, 1);
					//heightOfComment = 2;
					currentPageHeight += 3;
				}
				
				heightOfComment += dispStr(comment, normfont, 2*(str[i+1]-49), currentPageHeight-posY[1], k);
				for (k = 0; k < 2*(str[i+1]-49); k+=2) {
					ML_vertical_line(k, currentPageHeight-2-posY[1], currentPageHeight+heightOfComment-posY[1]-2, 1);
				}
				//if (k > 2) ML_vertical_line(k, currentPageHeight-posY[1], currentPageHeight+heightOfComment+4-posY[1], 1);
				currentPageHeight += heightOfComment;
				i = j;
			}
		}
	}
}

//interprets the html and the tags
void dispPost(unsigned char* str, int strlen) {
	int i;
	int currentPostRank = 0;
	int currentPageHeight = 13;
	ML_horizontal_line(0-posY[0], 0, 127, 1);
	ML_horizontal_line(10-posY[0], 0, 127, 1);
	
	for (i = 0; i < strlen; i++) {
		if (str[i] == '<') {
			if (str[i+1] == 'p') { //it is a post
				int j = i+2;
				int heightOfPost = 0;
				unsigned char title[512] = {0}; //because f* dynamic allocation, max title size is 300 characters
				int k = 0;
				int strlen = sizeof(title);
				while (str[j] != '>') { //as long as the post tag doesn't end
					/*while (str[j] == ' ') //loops through the next attribute
						j++;
					
					if (str[j] == 't') { //title attribute*/
						
						
						//while (str[j] != '"') {
							//j += 2;
							title[k] = str[j];
							k++;
							j++;
						//}
						
					/*}
					if (str[j] == 'a') {
						unsigned char attributes[64] = {0};
						int k = 0;
						int strlen = sizeof(attributes);
						int test;
						j+=2;
						while (str[j] != '"') {
							attributes[k] = str[j];
							k++;
							j++;
						}
						test = dispStr(attributes, normfont, 0, (currentPageHeight+heightOfPost)-posY[0], k);
						heightOfPost += test;
						j++;
					}
					if (str[j] != ' ' && str[j] != 't' && str[j] != 'a' && str[j] != 's') {
						break;
					}*/
				}
				
				heightOfPost += dispStr(title, normfont, 0, currentPageHeight-posY[0], k);
				j++;
				
				postHeights[currentPostRank] = heightOfPost;
					
				ML_horizontal_line(currentPageHeight+postHeights[currentPostRank]-1-posY[0], 0, 127, 1);
				
				currentPageHeight += postHeights[currentPostRank]+3;
				currentPostRank++;
				i = j;
			}
			if (str[i+1] == 'r') {
				int j = i+3;
				int k = 0;
				int l;
				int subLength = 0;
				unsigned char subreddit[64] = {0};
				while (str[j] != '>') {
					subreddit[k] = str[j];
					k++;
					j++;
				}
				for (l = 0; l < k; l++)
					subLength += normfont.length[subreddit[l]-32] +1;
				
				dispStr(subreddit, normfont, 65-(subLength/2), 3-posY[0], k);
			}
		}
	}
}

//displays a given string, using a given font, at the given coordinates
//returns the height of the string
int dispStr(unsigned char* str, struct Font font, int x2, int y, int strlen) {
	int k;
	int x = x2;
	int y2 = y;
	for (k=0; k < strlen; k++) { //browses through the given string
	
		//word wrap: if the current character isn't a space, simply display it
		if (str[k] != 32 && str[k] != '\0' && str[k] != '\n') {
			//if (option = 1) {
				long j = 1 << (6*font.length[str[k]-32])-1; //initializes a long for bit checking. The long is equal to 0b10000.. with number of zeroes being the maximum length of the character, minus 1 because there's already a 1.
				char i;
				
				for (i = 0; i < 6*font.length[str[k]-32]; i++) { //browses through the pixels of the character specified, shifting the 1 of j to the right each time, so that it makes 0b01000.., 0b001000... etc
				
					if (font.ltr[str[k]-32] & (j >> i)) { //checks if the bit that is a 1 in the j is also a 1 in the character
					
						ML_pixel(x+i%(font.length[str[k]-32]), y+i/font.length[str[k]-32], 1); //if so, locates the pixel at the coordinates, using modulo and division to calculate the coordinates relative to the top left of the character
					}
				}
			//}
			
			x += font.length[str[k]-32] + 1; //now that the character has been fully displayed, shifts the cursor right by the length of character + 1
		} else if (str[k] == '\n') {
			y += 7;
			x = x2;
		} else if (str[k] == ' ') { //the current character is a space, so see if it manages to display the word without going over x=128
			
			int i = x+4; //initializes an int to count the number of total pixels the next word takes
			int j = k+1; //initializes the char to the current char+1 (which is always another character)
			while (str[j] != 32 && str[j] != '\0') { //as long as it doesn't encounter another space or end of string
				i += font.length[str[j]-32]+1; //it increments i by the length of the character + 1
				j++;
			}
			
			if (i > 128) { //the word can't be displayed, note that it is STRICTLY superior because we added an unnecessary pixel at the end
				y += 7; //goes on next line which is 8 pixels down
				x = x2; //puts cursor on beginning of line
			} else {
				x += 4;
			}
		}
	}
	return y+8-y2;
}
//****************************************************************************
//**************                                              ****************
//**************                 Notice!                      ****************
//**************                                              ****************
//**************  Please do not change the following source.  ****************
//**************                                              ****************
//****************************************************************************


#pragma section _BR_Size
unsigned long BR_Size;
#pragma section


#pragma section _TOP

//****************************************************************************
//  InitializeSystem
//
//  param   :   isAppli   : 1 = Application / 0 = eActivity
//              OptionNum : Option Number (only eActivity)
//
//  retval  :   1 = No error / 0 = Error
//
//****************************************************************************
int InitializeSystem(int isAppli, unsigned short OptionNum)
{
    return INIT_ADDIN_APPLICATION(isAppli, OptionNum);
}
#pragma section