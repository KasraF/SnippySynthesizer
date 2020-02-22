def get_middle(s):
    rs = s[(len(s)-1)//2:len(s)//2+1]
    return rs

get_middle("test") == "es"
get_middle("testing") == "t"
get_middle("middle") =="dd"
get_middle("A") == "A"
get_middle("of") == "of"