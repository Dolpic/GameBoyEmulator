package ch.epfl.gameboj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Opcode;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public class S6Tests {

    private enum RegList implements Register {
        PC,SP,A,F,B,C,D,E,H,L
     }
     
     private Bus connect(Cpu cpu, Ram ram) {
         RamController rc = new RamController(ram, 0);
         Bus b = new Bus();
         cpu.attachTo(b);
         rc.attachTo(b);
         return b;
     }
     
     private void cycle(Cpu c, Timer t, long cycle) {
         t.cycle(cycle);
         c.cycle(cycle);
     }
     
     @Test
     void test_TIMER() {
         Cpu c = new Cpu();
         Ram r = new Ram(0xFFFF);
         Bus b = connect(c, r);
         Timer t = new Timer(c);
         t.attachTo(b);
         
         c.write(0xFFFF, 0b0000_1111);
         
         byte[] prog = new byte[] {
                 (byte)Opcode.LD_A_N8.encoding, (byte)0xFE,
                 (byte)Opcode.LD_N16R_A.encoding, (byte)0x06,(byte)0xFF, //reg_TMA
                 (byte)Opcode.LD_A_N8.encoding, (byte)0b0101,
                 (byte)Opcode.LD_N16R_A.encoding, (byte)0x07,(byte)0xFF, //reg_TAC
                 (byte)Opcode.LD_A_N16R.encoding, (byte)0x04,(byte)0xFF, //reg_DIV
               };
       

         for(int i=0; i<prog.length; i++) {
             b.write(i, Bits.clip(8,prog[i]));
         }
         

         cycle(c,t,0);
         cycle(c,t,1);
         
         cycle(c,t,2);
         cycle(c,t,3);
         cycle(c,t,4);
         cycle(c,t,5);
         
         cycle(c,t,6);
         cycle(c,t,7);
         
         cycle(c,t,8);
         cycle(c,t,9);
         cycle(c,t,10);
         cycle(c,t,11);
         
         cycle(c,t,12);
         cycle(c,t,13);
         cycle(c,t,14);
         cycle(c,t,15);
         
         assertEquals(0, c._testGetPcSpAFBCDEHL()[RegList.A.index()]);
         assertEquals(0xFE, t._testGetTMA());
         assertEquals(0b0101, t._testGetTAC());
         assertEquals(2, t._testGetTIMA());
     }
}
