#include "fonts.h"

/*Here is how a character is defined.

Each character is 7 pixels high. The length is defined by you in the length array.

Because some characters (ex: m, M) are 5 px wide, the total amount is >32 bit,
so the character is stored in an array of 2 longs. The first long is only used if
the character is >32 bit.

For example, the character 'M', of length 3, is the following in binary:

00000
10001
11011
10101
10001
10001
00000

You must not forget the "00000" at the end, else your character will be shifted down by a pixel.
Now just remove the newlines in the character. You should get: 00000100011101110101100011000100000
Put that in a binary to decimal converter and you've got your number representing the character.

Also, make sure to define the correct length, else it will display gibberish.
*/

struct Font normfont = {
	{{	//letters
			0 //space
},{0,		58 //!
},{0,		184320 //" replaced by 0x81
},{0,		368409920 //#
},{0,		524752832 //$
},{0,		136456 //%
},{0,		6838992 //&
},{0,		48 //'
},{0,		1700 //(
},{0,		2392 //)
},{0,		174592 //*
},{0,		11904 //+
},{0,		3 //
},{0,		3584 //-
},{0,		2 //.
},{0,		38176 ///
},{0,		252792 //0
},{0,		206008 //1
},{0,		237368 //2
},{0,		235128 //3
},{0,		187976 //4
},{0,		249464 //5
},{0,		249720 //6
},{0,		234640 //7
},{0,		253816 //8
},{0,		253560 //9
},{0,		10 //:
},{0,		11 //;
},{0,		43144 //< replaced by 0x7F
},{0,		29120 //=
},{0,		139936 //> replaced by 0x80
},{0,		201744 //?
},{0,		488035776 //@
},{0,		6922128 //A
},{0,		15329760 //B
},{0,		6916448 //C
},{0,		15309280 //D
},{0,		16312560 //E
},{0,		16312448 //F
},{0,		7911776 //G
},{0,		10090896 //H
},{0,		238776 //I
},{0,		7480000 //J
},{0,		10144400 //K
},{0,		8947952 //L
},{0,		599442976 //M
},{0,		10336656 //N
},{0,		6920544 //O
},{0,		15310464 //P
},{0,		6921072 //Q
},{0,		15310480 //R
},{0,		7889376 //S
},{0,		238736 //T
},{0,		10066288 //U
},{0,		588818560 //V
},{0,		588961088 //W
},{0,		185704 //X
},{0,		187024 //Y
},{0,		15878384 //Z
},{0,		3756 //[
},{0,		148552 //backslash
},{0,		3420 //]
},{0,		86016 //^
},{0,		7 //_
},{0,		3648 //`
},{0,		15192 //a
},{0,		158576 //b
},{0,		14616 //c
},{0,		47960 //d
},{0,		15256 //e
},{0,		118048 //f
},{0,		15310 //g
},{0,		158568 //h
},{0,		46 //i
},{0,		1111 //j
},{0,		154984 //k
},{0,		62 //l
},{0,		27973280 //m
},{0,		27496 //n
},{0,		11088 //o
},{0,		27508 //p
},{0,		15193 //q
},{0,		23840 //r
},{0,		924 //s
},{0,		2988 //t
},{0,		23416 //u
},{0,		23376 //v
},{0,		18535744 //w
},{0,		21864 //x
},{0,		23246 //y
},{0,		30008 //z
},{0,		108696 //{
},{0,		62 //|
},{0,		205488 //}
},{0,		448512 //~
},{0,		43144 //< 0x7F
},{0,		139936 //> 0x80
},{0,		184320 //" 0x81
},{0,		50022784 //look of disapproval 0x82
},{0,		496640000 //lenny face eye 0x83
},{0,		138482222 //lenny face nose/mouth 0x84
},{0,		4088 //sunglasses 0x85
},{0,		3840 //*puts on sunglasses* 0x86
},{0,		229376 //overline 0x87
},{0,		693142620 //shrug face 0x88
},{0,		801688 //é 0x89

},{0,		201744 //for some reason, when the dispStr() function seeks the character at 0x90 and above
},{0,		201744 //it actually seeks it 6 characters after. there is absolutely no logical way I can think of
},{0,		201744 //that causes this bug. 0x90 (144) is not a significant number. neither is 6.
},{0,		201744 //so rather than spending 4532 hours debugging, the fix is simple:
},{0,		201744 //just put 6 characters between 0x89 and 0x90.
},{0,		201744

},{0,		1588120 //è 0x90
},{0,		1588056 //à 0x91
},{0,		1596280 //ù 0x92
},{0,		14622 //ç 0x93
},{0,		1617408 //² 0x94
},{0,		55501872 //€ 0x95
},{0,		1048716256 //somme 0x96

		
	}},{ //lengths
		3, //space
		1, //!
		3, //"
		5, //#
		5, //$
		3, //%
		4, //&
		1, //'
		2, //(
		2, //)
		3, //*
		3, //+
		1, //,
		3, //-
		1, //.
		3, ///
		3, //0
		3, //1
		3, //2
		3, //3
		3, //4
		3, //5
		3, //6
		3, //7
		3, //8
		3, //9
		1, //:
		1, //;
		3, //<
		3, //=
		3, //>
		3, //?
		5, //@
		4, //A
		4, //B
		4, //C
		4, //D
		4, //E
		4, //F
		4, //G
		4, //H
		3, //I
		4, //J
		4, //K
		4, //L
		5, //M
		4, //N
		4, //O
		4, //P
		4, //Q
		4, //R
		4, //S
		3, //T
		4, //U
		5, //V
		5, //W
		3, //X
		3, //Y
		4, //Z
		2, //[
		3, //backslash
		2, //]
		3, //^
		3, //_
		2, //`
		3, //a
		3, //b
		3, //c
		3, //d
		3, //e
		3, //f
		3, //g
		3, //h
		1, //i
		2, //j
		3, //k
		1, //l
		5, //m
		3, //n
		3, //o
		3, //p
		3, //q
		3, //r
		2, //s
		2, //t
		3, //u
		3, //v
		5, //w
		3, //x
		3, //y
		3, //z
		3, //{
		1, //|
		3, //}
		5, //~
		3, //<
		3, //>
		3, //"
		5, //look of disapproval
		5, //lenny face eye
		5, //lenny face nose/mouth
		3, //sunglasses
		3, //*puts on sunglasses*
		3, //overline
		5, //shrug face
		3, //é
		3, //unused lengths, see letters comment
		3,
		3,
		3,
		3,
		3,
		3, //è
		3, //à
		3, //ù
		3, //ç
		3, //²
		4, //€
		5, //somme
		3,
		3,
		3,
		3,
		3,
		3,
		3,
		3,
		3,
		3,
		3
	}
};

//displays a given string, using a given font, at the given coordinates
//returns the height of the string
int dispStr(unsigned char* str, int x2, int y) {
	int k = 0;
	int x = x2;
	int y2 = y;
	do { //browses through the given string
	
		//word wrap: if the current character isn't a space, simply display it
		if (str[k] != ' ' && str[k] != '\0' && str[k] != '\n') {
			if (y >= -6 && y < 68) {
				
				int charlength = normfont.length[str[k]-32];
				unsigned long j = 1 << ((7*charlength)%32)-1; //initializes a long for bit checking. The long is equal to 0b10000.. with number of zeroes being the maximum length of the character, minus 1 because there's already a 1.
				char i;
				
				for (i = 0; i < 7*charlength; i++) { //browses through the pixels of the character specified, shifting the 1 of j to the right each time, so that it makes 0b01000.., 0b001000... etc
					
					if (normfont.ltr[str[k]-32][1-(7*charlength-i)/33] & j) { //checks if the bit that is a 1 in the j is also a 1 in the character
					
						ML_pixel(x+i%(charlength), y+i/charlength, 1); //if so, locates the pixel at the coordinates, using modulo and division to calculate the coordinates relative to the top left of the character
					}
					
					if (j != 1)
						j >>= 1;
					else
						j = 2147483648;
					
				}
			}
			
			x += normfont.length[str[k]-32] + 1; //now that the character has been fully displayed, shifts the cursor right by the length of character + 1
		} else if (str[k] == '\n') {
			y += 8;
			x = x2;
		} else if (str[k] == ' ') { //the current character is a space, so see if it manages to display the word without going over x=128
			
			int i = x+4; //initializes an int to count the number of total pixels the next word takes
			int j = k+1; //initializes the char to the current char+1 (which is always another character)
			while (str[j] != 32 && str[j] != '\0' && str[j] != '\n') { //as long as it doesn't encounter another space or end of string
				i += normfont.length[str[j]-32]+1; //it increments i by the length of the character + 1
				j++;
			}
			
			if (i > 128) { //the word can't be displayed, note that it is STRICTLY superior because we added an unnecessary pixel at the end
				y += 8; //goes on next line which is 8 pixels down
				x = x2; //puts cursor on beginning of line
			} else {
				x += 4;
			}
		}
		k++;
	} while (str[k] != '\0');
	return y+8-y2;
}