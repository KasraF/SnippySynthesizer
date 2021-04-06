def makeHeatmaps(lines):
    correct = [[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]
    count = [[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]
    terminated = [[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0],[0,0,0,0,0]]
    
    for l in lines:
        sp = l.split(',')
        ex = int(sp[1])-1
        it = int(sp[2])-1
        res = sp[3]
        count[ex][it] += 1
        if res == '+':
            correct[ex][it] += 1
            terminated[ex][it] += 1
        elif res == '-':
            terminated[ex][it] += 1
    print("Correctness:")
    print(",1,2,3,4,5")
    for ex in range(4):
        print(str(ex+1) + "," + ",".join([str(float(correct[ex][it])/count[ex][it]) for it in range(5)]))
    print("Termination:")
    print(",1,2,3,4,5")
    for ex in range(4):
        print(str(ex+1) + "," + ",".join([str(float(terminated[ex][it])/count[ex][it]) for it in range(5)]))
