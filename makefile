JVC =  javac
JVCFLAGS =
.SUFFIXES : .class .java
.java.class :
	$(JVC) $(JVCFLAGS) $<

PRG=panserver.class panclient.class 
all:    $(PRG)
	@chmod 755 ${PRG}

panserver: \
	 panserver.class
panserver.class: panserver.java

panclient: \
         panclient.class
panclient.class: panclient.java
clean:
	rm *.class 