struct Font {
	long ltr[130]; //an array of longs for each character
	char length[130]; //array of chars for the length of each character
};

/*

Here is how a character is defined.

Each character is 6 pixels high. The length is defined by you in the length array.

For example, the character '0', of length 3, is the following in binary:

111
101
101
101
111
000

You must not forget the "000" at the end, else your character will be shifted down by a pixel.
Now just remove the newlines in the character. You should get: 111101101101111000
Put that in a binary to decimal converter and you've got your number representing the character.

Also, make sure to define the correct length, else it will display gibberish.

*/

struct Font normfont = {
	{ //letters
		0, //space
		58, //!
		184320, //", replaced by 0x81
		368409920, //#
		524752832, //$
		136456, //%
		6838992, //&
		48, //'
		1700, //(
		2392, //)
		174592, //*
		11904, //+
		3, //,
		3584, //-
		2, //.
		38176, ///
		252792, //0
		206008, //1
		237368, //2
		235128, //3
		187976, //4
		249464, //5
		249720, //6
		234640, //7
		253816, //8
		253560, //9
		10, //:
		11, //;
		43144, //<, replaced by 0x7F
		29120, //=
		139936, //>, replaced by 0x80
		201744, //?
		488035776, //@
		6922128, //A
		15329760, //B
		6916448, //C
		15309280, //D
		16312560, //E
		16312448, //F
		7911776, //G
		10090896, //H
		238776, //I
		7480000, //J
		10144400, //K
		8947952, //L
		599442976, //M
		10336656, //N
		6920544, //O
		15310464, //P
		6921072, //Q
		15310480, //R
		7889376, //S
		238736, //T
		10066288, //U
		588818560, //V
		588961088, //W
		185704, //X
		187024, //Y
		15878384, //Z
		3756, //[
		148552, //backslash
		3420, //]
		86016, //^
		7, //_
		3648, //`
		15192, //a
		158576, //b
		14616, //c
		47960, //d
		15256, //e
		118048, //f
		15310, //g
		158568, //h
		46, //i
		1111, //j
		154984, //k
		62, //l
		27973280, //m
		27496, //n
		11088, //o
		27508, //p
		15193, //q
		23840, //r
		924, //s
		2988, //t
		23416, //u
		23376, //v
		18535744, //w
		21864, //x
		23246, //y
		30008, //z
		108696, //{
		62, //|
		205488, //}
		448512, //~
		43144, //<
		139936, //>
		184320, //"
		50022784, //look of disapproval
		496640000, //lenny face eye
		138482222, //lenny face nose/mouth
		4088, //sunglasses
		3840, //*puts on sunglasses*
		229376, //overline
		693142620, //shrug face
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0,
		0
		
	},{ //lengths
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
		0,
		0,
		0,
		0,
		0,
		0
	}
};

int dispStr(unsigned char* str, struct Font font, int x, int y, int strlen);