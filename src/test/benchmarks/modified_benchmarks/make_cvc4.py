import os
import random

def do_file(indir,outdir,filename):
    infile = os.path.join(indir,filename)
    outfile_base = filename[:-3]
    f = open(infile,"r")
    flines = f.readlines()
    f.close()
    constraintlines = [i for i, e in enumerate(flines) if e.strip().startswith('(constraint')]
    random.shuffle(constraintlines)
    for j,l in enumerate(constraintlines):
        newcontent = [e for i,e in enumerate(flines) if i != l]
        ofname = os.path.join(outdir,outfile_base + "_" + str(j) + ".sl")
        of = file(ofname,"w")
        of.writelines(newcontent)
        of.close()    

def do_indir(indir):
    for filename in os.listdir(indir):
        do_file(indir,outdir,filename)


indir1 = 'C:\\Users\\hila\\IdeaProjects\\partialCorrectness\\src\\test\\benchmarks\\modified_benchmarks\\contradiction'
indir2 = 'C:\\Users\\hila\\IdeaProjects\\partialCorrectness\\src\\test\\benchmarks\\modified_benchmarks\\returns_garbage'
outdir = 'C:\\Users\\hila\\IdeaProjects\\partialCorrectness\\src\\test\\benchmarks\\modified_benchmarks\\for_cvc4'

do_indir(indir1)
do_indir(indir2)
