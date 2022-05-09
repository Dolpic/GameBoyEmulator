package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * This (big) class emulate a Gameboy CPU.
 * All the arthemic unit is in Alu.java.
 * 
 * @author Corentin Junod (283214)
 */
public final class Cpu implements Component, Clocked {

/*** Statics attributes and methods ***/

    private static final int OPCODE_PREFIX = 0xCB;
    private static final int PC_NOT_CHANGED = -1;
    private static final int CYCLES_AFTER_DETECT_INTERRUPT = 5;
    
    // Two tables that retrieve an Opcode given an Opcode encoding
    private static final Opcode[] DIRECT_OPCODE_TABLE   = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);

    // Used to create the two tables above
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] res = new Opcode[Opcode.values().length];
        for (Opcode o : Opcode.values()) {
            if (o.kind == kind) res[o.encoding] = o;
        }
        return res;
    }

/*** Attributes and Enumerations ****/
    
    private Bus bus;
    private final RegisterFile<Reg> regFile;
    private final Ram highRam;

    private long nextNonIdleCycle;

    private int PC;    // Program Counter
    private int SP;    // Stack Pointer
    private int newPC; // New value of PC, for control instructions

    private boolean IME; // Interrupts Master Enabled
    private int IE;      // Interrupts Enabled
    private int IF;      // Interrupts Flags

    //Represents the different registers in the CPU
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }
    
    //Represents the pairs of above registers
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    //Used to indicated which Flag must be taken after an operation
    private enum FlagSrc {
        V0, V1, ALU, CPU
    }
    
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

/*** Public functions ****/ 
    
    /**
     * Create a new Gameboy CPU that reads the attached bus.
     * This CPU is controlled by the method "cycle".
     */
    public Cpu() {
        regFile = new RegisterFile<>(Reg.values());
        PC = 0;
        SP = 0;

        IME = false;
        IE = 0;
        IF = 0;

        highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    }

    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        if (cycle == nextNonIdleCycle || (nextNonIdleCycle == Long.MAX_VALUE && detectInterrupts() != null)) {
            newPC = PC_NOT_CHANGED;
            Opcode opcode = null;
            
            //Handle interruptions and end this cycle if needed
            if(handleInterrupts(cycle)) return;
            
            if (bus.read(PC) == OPCODE_PREFIX)
                opcode = PREFIXED_OPCODE_TABLE[bus.read(PC + 1)];
            else
                opcode = DIRECT_OPCODE_TABLE[bus.read(PC)];
            
            dispatch(opcode);

            if (nextNonIdleCycle != Long.MAX_VALUE) 
                nextNonIdleCycle += opcode.cycles;
            
            if (newPC == PC_NOT_CHANGED)
                PC = Bits.clip(16, PC + opcode.totalBytes);
            else
                PC = newPC;
        }
        // else we have nothing to do during this cycle
    }
    
    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        
        if (address == AddressMap.REG_IE)
            return IE;
        else if (address == AddressMap.REG_IF)
            return IF;
        else if (address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        else
            return NO_DATA;
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_IE)
            IE = data;
        else if (address == AddressMap.REG_IF)
            IF = data;
        else if (address >= AddressMap.HIGH_RAM_START && address < AddressMap.HIGH_RAM_END)
            highRam.write(address - AddressMap.HIGH_RAM_START, data);
        //else nothing to do
    }

    /* (non-Javadoc)
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = Objects.requireNonNull(bus);
        bus.attach(this);
    }

    /**
     * Request an interrupt to the processor.
     * 
     * @param i
     *            a member of Interrupt, indicate the interruption to request
     */
    public void requestInterrupt(Interrupt i) {
        IF |= i.mask();
    }
    
    
/*** Private functions ****
 * 
 * As we already check the values in the Bus, we don't have to use Preconditions here.
 * We ignore the fact that PC and address can be greater
 * as 0xFFFF, this situation never happends in a valid program.
 */

    private void dispatch(Opcode opcode) {
        
        switch (opcode.family) {
        case NOP: break;
        
        // From bus/raw to register
        case LD_R8_HLR: {
            regFile.set(extractReg(opcode, 3), read8AtHl());
        }break;
        case LD_A_HLRU: {
            regFile.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, getReg16(Reg16.HL) + extractHlIncrement(opcode));                                                                         
        }break;
        case LD_A_N8R: {
            regFile.set(Reg.A, read8(AddressMap.REGS_START + read8AfterOpcode()));
        }break;
        case LD_A_CR: {
            regFile.set(Reg.A, read8(AddressMap.REGS_START + regFile.get(Reg.C)));
        }break;
        case LD_A_N16R: {
            regFile.set(Reg.A, read8(read16AfterOpcode()));
        }break;
        case LD_A_BCR: {
            regFile.set(Reg.A, read8(getReg16(Reg16.BC)));
        }break;
        case LD_A_DER: {
            regFile.set(Reg.A, read8(getReg16(Reg16.DE)));
        }break;
        case LD_R8_N8: {
            regFile.set(extractReg(opcode, 3), read8AfterOpcode());
        }break;
        case LD_R16SP_N16: {
            setReg16SP(extractReg16(opcode), read16AfterOpcode());
        }break;
        case POP_R16: {
            setReg16(extractReg16(opcode), pop16());
        }break;

        // From register to bus
        case LD_HLR_R8: {
            write8AtHl(regFile.get(extractReg(opcode, 0)));
        }break;
        case LD_HLRU_A: {
            write8AtHl(regFile.get(Reg.A));
            setReg16(Reg16.HL, getReg16(Reg16.HL) + extractHlIncrement(opcode));
        }break;
        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(), getRegAValue());
        }break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + regFile.get(Reg.C), getRegAValue());
        }break;
        case LD_N16R_A: {
            write8(read16AfterOpcode(), getRegAValue());
        }break;
        case LD_BCR_A: {
            write8(getReg16(Reg16.BC), getRegAValue());
        }break;
        case LD_DER_A: {
            write8(getReg16(Reg16.DE), getRegAValue());
        }break;
        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        }break;
        case LD_N16R_SP: {
            write16(read16AfterOpcode(), SP);
        }break;
        case PUSH_R16: {
            push16(getReg16(extractReg16(opcode)));
        }break;

        // From register to register
        case LD_R8_R8: {
            regFile.set(extractReg(opcode, 3), regFile.get(extractReg(opcode, 0)));
        }break;
        case LD_SP_HL: {
            SP = getReg16(Reg16.HL);
        }break;

        // Additions
        case ADD_A_R8: {
            int operation = Alu.add(getRegAValue(), regFile.get(extractReg(opcode, 0)), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case ADD_A_N8: {
            int operation = Alu.add(getRegAValue(), read8AfterOpcode(), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case ADD_A_HLR: {
            int operation = Alu.add(getRegAValue(), read8AtHl(), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case INC_R8: {
            int operation = Alu.add(regFile.get(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), operation);
            combineAluFlags(operation, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        }break;
        case INC_HLR: {
            int operation = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(operation));
            combineAluFlags(operation, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
        }break;
        case INC_R16SP: {
            int operation = Alu.add16H(getReg16SP(extractReg16(opcode)), 1);
            setReg16SP(extractReg16(opcode), Alu.unpackValue(operation));
        }break;
        case ADD_HL_R16SP: {
            int operation = Alu.add16H(getReg16(Reg16.HL), getReg16SP(extractReg16(opcode)));
            setReg16SP(Reg16.HL, Alu.unpackValue(operation));
            combineAluFlags(operation, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        }break;
        case LD_HLSP_S8: { // This family contains ADD SP, e8 and LD HL, SP + e8
            int operation = Alu.add16L(SP, Bits.clip(16, Bits.signExtend8(read8AfterOpcode())));
            setReg16SP(Bits.test(opcode.encoding, 4) ? Reg16.HL : Reg16.AF, Alu.unpackValue(operation)); // Here Reg16.AF means SP
            combineAluFlags(operation, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
        }break;

        // Subtractions
        case SUB_A_R8: {
            int operation = Alu.sub(getRegAValue(), regFile.get(extractReg(opcode, 0)), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case SUB_A_N8: {
            int operation = Alu.sub(getRegAValue(), read8AfterOpcode(), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case SUB_A_HLR: {
            int operation = Alu.sub(getRegAValue(), read8AtHl(), getCarryValue(opcode));
            setRegFlags(Reg.A, operation);
        }break;
        case DEC_R8: {
            int operation = Alu.sub(regFile.get(extractReg(opcode, 3)), 1);
            setRegFromAlu(extractReg(opcode, 3), operation);
            combineAluFlags(operation, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        }break;
        case DEC_HLR: {
            int operation = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(operation));
            combineAluFlags(operation, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
        }break;
        case CP_A_R8: {
            int operation = Alu.sub(getRegAValue(), regFile.get(extractReg(opcode, 0)));
            setFlags(operation);
        }break;
        case CP_A_N8: {
            setFlags(Alu.sub(getRegAValue(), read8AfterOpcode()));
        }break;
        case CP_A_HLR: {
            setFlags(Alu.sub(getRegAValue(), read8AtHl()));
        }break;
        case DEC_R16SP: {
            setReg16SP(extractReg16(opcode), getReg16SP(extractReg16(opcode)) - 1);
        }break;

        // And, or, xor, complement
        case AND_A_R8: {
            int operation = Alu.and(getRegAValue(), regFile.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, operation);
        }break;
        case AND_A_N8: {
            setRegFlags(Reg.A, Alu.and(getRegAValue(), read8AfterOpcode()));
        }break;
        case AND_A_HLR: {
            setRegFlags(Reg.A, Alu.and(getRegAValue(), read8AtHl()));
        }break;
        case OR_A_R8: {
            int operation = Alu.or(getRegAValue(), regFile.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, operation);
        }break;
        case OR_A_N8: {
            setRegFlags(Reg.A, Alu.or(getRegAValue(), read8AfterOpcode()));
        }break;
        case OR_A_HLR: {
            setRegFlags(Reg.A, Alu.or(getRegAValue(), read8AtHl()));
        }break;
        case XOR_A_R8: {
            int operation = Alu.xor(getRegAValue(), regFile.get(extractReg(opcode, 0)));
            setRegFlags(Reg.A, operation);
        }break;
        case XOR_A_N8: {
            setRegFlags(Reg.A, Alu.xor(getRegAValue(), read8AfterOpcode()));
        }break;
        case XOR_A_HLR: {
            setRegFlags(Reg.A, Alu.xor(getRegAValue(), read8AtHl()));
        }break;
        case CPL: {
            regFile.set(Reg.A, Bits.complement8(regFile.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
        }break;

        // Rotations, shifts
        case ROTCA: {
            int operation = Alu.rotate(extractRotDir(opcode), getRegAValue());
            setRegFromAlu(Reg.A, operation);
            combineAluFlags(operation, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,FlagSrc.ALU);
        }break;
        case ROTA: {
            int operation = Alu.rotate(extractRotDir(opcode), getRegAValue(), Bits.test(regFile.get(Reg.F), Flag.C.index()));
            setRegFromAlu(Reg.A, operation);
            combineAluFlags(operation, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
        }break;
        case ROTC_R8: {
            int operation = Alu.rotate(extractRotDir(opcode), regFile.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case ROT_R8: {
            int operation = Alu.rotate(extractRotDir(opcode), regFile.get(extractReg(opcode, 0)), Bits.test(regFile.get(Reg.F), Flag.C.index()));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case ROTC_HLR: {
            int operation = Alu.rotate(extractRotDir(opcode), read8AtHl());
            write8AtHlAndSetFlags(operation);
        }break;
        case ROT_HLR: {
            int operation = Alu.rotate(extractRotDir(opcode), read8AtHl(), Bits.test(regFile.get(Reg.F), Flag.C.index()));
            write8AtHlAndSetFlags(operation);
        }break;
        case SWAP_R8: {
            int operation = Alu.swap(regFile.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case SWAP_HLR: {
            write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
        }break;
        case SLA_R8: {
            int operation = Alu.shiftLeft(regFile.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case SRA_R8: {
            int operation = Alu.shiftRightA(regFile.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case SRL_R8: {
            int operation = Alu.shiftRightL(regFile.get(extractReg(opcode, 0)));
            setRegFlags(extractReg(opcode, 0), operation);
        }break;
        case SLA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftLeft(read8AtHl()));
        } break;
        case SRA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightA(read8AtHl()));
        } break;
        case SRL_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightL(read8AtHl()));
        }break;

        // Tests and Sets on bits
        case BIT_U3_R8: {
            boolean res = Bits.test(regFile.get(extractReg(opcode, 0)), getBitIndex(opcode));
            combineAluFlags(0, res ? FlagSrc.V0 : FlagSrc.V1, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        }break;
        case BIT_U3_HLR: {
            boolean res = Bits.test(read8AtHl(), getBitIndex(opcode));
            combineAluFlags(0, res ? FlagSrc.V0 : FlagSrc.V1, FlagSrc.V0, FlagSrc.V1, FlagSrc.CPU);
        }break;
        case CHG_U3_R8: {
            int operation = 0;
            if (Bits.test(opcode.encoding, 6)) 
                operation = Alu.or( regFile.get(extractReg(opcode, 0)), 1 << getBitIndex(opcode));
            else 
                operation = Alu.and(regFile.get(extractReg(opcode, 0)), Bits.clip(8, ~(1 << getBitIndex(opcode))));
            
            setRegFromAlu(extractReg(opcode, 0), operation);
        }break;
        case CHG_U3_HLR: {
            int operation = 0;
            if (Bits.test(opcode.encoding, 6)) 
                operation = Alu.or(read8AtHl(), 1 << getBitIndex(opcode));
            else 
                operation = Alu.and(read8AtHl(), Bits.clip(8, ~(1 << getBitIndex(opcode))));
            
            write8AtHl(Alu.unpackValue(operation));
        }break;

        // Misc. ALU
        case DAA: {
            int valueF = regFile.get(Reg.F);
            int operation = Alu.bcdAdjust(getRegAValue(), 
                                          Bits.test(valueF, Flag.N.index()),
                                          Bits.test(valueF, Flag.H.index()), 
                                          Bits.test(valueF, Flag.C.index()));
            setRegFromAlu(Reg.A, operation);
            combineAluFlags(operation, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);
        }break;
        case SCCF: {
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, getCarryValue(opcode) ? FlagSrc.V0 : FlagSrc.V1);
        }break;

        // Jumps
        case JP_HL: {
            newPC = getReg16(Reg16.HL);
        }break;
        case JP_N16: {
            newPC = read16AfterOpcode();
        }break;
        case JP_CC_N16: {
            if (extractResultOfCondition(opcode)) newPC = read16AfterOpcode();
        }break;
        case JR_E8: {
            newPC = Bits.clip(16, PC + opcode.totalBytes + Bits.signExtend8(read8AfterOpcode()));
        }break;
        case JR_CC_E8: {
            if (extractResultOfCondition(opcode))
                newPC = Bits.clip(16, PC + opcode.totalBytes + Bits.signExtend8(read8AfterOpcode()));
        }break;

        // Calls and returns
        case CALL_N16: {
            push16(PC + opcode.totalBytes);
            newPC = read16AfterOpcode();
        }break;
        case CALL_CC_N16: {
            if (extractResultOfCondition(opcode)) {
                push16(PC + opcode.totalBytes);
                newPC = read16AfterOpcode();
            }
        }break;
        case RST_U3: {
            push16(PC + opcode.totalBytes);
            newPC = AddressMap.RESETS[Bits.extract(opcode.encoding, 3, 3)];
        }break;
        case RET: {
            newPC = pop16();
        }break;
        case RET_CC: {
            if (extractResultOfCondition(opcode)) newPC = pop16();
        }break;

        // Interruptions
        case EDI: {
            IME = Bits.test(opcode.encoding, 3);
        }break;
        case RETI: {
            IME = true;
            newPC = pop16();
        }break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        }break;
        case STOP:
            throw new Error("STOP is not implemented");
        default:
            throw new NullPointerException();
        }
    }

    
    /*********** Read / Write *********/
    
    private int read8(int address) {
        return bus.read(address);
    }

    private int read8AtHl() {
        return read8(getReg16(Reg16.HL));
    }

    private int read8AfterOpcode() {
        return bus.read(PC + 1);
    }

    private int read16(int address) {
        return Bits.make16(read8(address + 1), read8(address));
    }

    private int read16AfterOpcode() {
        return Bits.make16(read8(PC + 2), read8(PC + 1));
    }

    private void write8(int address, int v) {
        bus.write(address, v);
    }

    private void write16(int address, int v) {
        bus.write(address, Bits.clip(8, v));
        bus.write(address + 1, Bits.clip(8, v >> 8));
    }

    private void write8AtHl(int v) {
        bus.write(getReg16(Reg16.HL), v);
    }

    private void push16(int v) {
        SP = Bits.clip(16, SP - 2);
        write16(SP, v);
    }

    private int pop16() {
        int ret = read16(SP);
        SP = Bits.clip(16, SP + 2);
        return ret;
    }

    /*********** 16 bits Registers Functions *********/
    private int getReg16(Reg16 r) {
        switch (r) {
            case AF: return ((regFile.get(Reg.A) << 8) | regFile.get(Reg.F));
            case BC: return ((regFile.get(Reg.B) << 8) | regFile.get(Reg.C));
            case DE: return ((regFile.get(Reg.D) << 8) | regFile.get(Reg.E));
            case HL: return ((regFile.get(Reg.H) << 8) | regFile.get(Reg.L));
        }
        throw new NullPointerException();
    }

    private int getReg16SP(Reg16 r) {
        if (r == Reg16.AF) return SP;
        else return getReg16(r);
    }

    private void setReg16(Reg16 r, int newV) {
        
        int low  = Bits.clip(8, newV);
        int high = Bits.clip(8, newV >> 8);

        switch (r) {
        case AF:
            regFile.set(Reg.A, high);
            regFile.set(Reg.F, low & 0b1111_0000); // The 4 LSB must be 0
            break;
        case BC:
            regFile.set(Reg.B, high);
            regFile.set(Reg.C, low);
            break;
        case DE:
            regFile.set(Reg.D, high);
            regFile.set(Reg.E, low);
            break;
        case HL:
            regFile.set(Reg.H, high);
            regFile.set(Reg.L, low);
            break;
        default:
            throw new NullPointerException();
        }
    }

    private void setReg16SP(Reg16 r, int newV) {
        newV = Bits.clip(16, newV);
        if (r == Reg16.AF) SP = newV;
        else setReg16(r, newV);
    }

   
    /*********** Extraction Functions ********/
   
    private Reg extractReg(Opcode opcode, int startBit) {
        switch (Bits.extract(opcode.encoding, startBit, 3)) {
            case 0b000: return Reg.B;
            case 0b001: return Reg.C;
            case 0b010: return Reg.D;
            case 0b011: return Reg.E;
            case 0b100: return Reg.H;
            case 0b101: return Reg.L;
            case 0b110: return null; //Unvalid case that will create an error
            case 0b111: return Reg.A;
        }
        throw new NullPointerException();
    }

    private Reg16 extractReg16(Opcode opcode) {
        switch (Bits.extract(opcode.encoding, 4, 2)) {
            case 0b00: return Reg16.BC;
            case 0b01: return Reg16.DE;
            case 0b10: return Reg16.HL;
            case 0b11: return Reg16.AF;
        }
        throw new NullPointerException();
    }

    private int extractHlIncrement(Opcode opcode) {
        return Bits.test(opcode.encoding, 4) ? -1 : 1;
    }

    private Alu.RotDir extractRotDir(Opcode opcode) {
        if (Bits.test(opcode.encoding, 3))
            return Alu.RotDir.RIGHT;
        else
            return Alu.RotDir.LEFT;
    }

    private int getBitIndex(Opcode opcode) {
        return Bits.extract(opcode.encoding, 3, 3);
    }

    private boolean getCarryValue(Opcode opcode) {
        return (Bits.test(opcode.encoding, 3) && Bits.test(regFile.get(Reg.F), Alu.Flag.C.index()));
    }

    private int getRegAValue() {
        return regFile.get(Reg.A);
    }

    private boolean extractResultOfCondition(Opcode opcode) throws Error {
        boolean result = false;
        switch (Bits.extract(opcode.encoding, 3, 2)) {
        case 0b00:
            result = !getFlag(Flag.Z); // NZ
            break;
        case 0b01:
            result = getFlag(Flag.Z);  // Z
            break;
        case 0b10:
            result = !getFlag(Flag.C); // NC
            break;
        case 0b11:
            result = getFlag(Flag.C);  // C
            break;
        default:
            throw new Error("Unauthorized value for condition");
        }
        if (result) nextNonIdleCycle += opcode.additionalCycles;
        return result;
    }

    /*********** Alu & Flags Functions *********/
    
    private void setRegFromAlu(Reg r, int vf) {
        regFile.set(r, Alu.unpackValue(vf));
    }

    private void setFlags(int vf) {
        regFile.set(Reg.F, Alu.unpackFlags(vf));
    }

    private boolean getFlag(Flag f) {
        return Bits.test(regFile.get(Reg.F), f.index());
    }

    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }

    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        setFlags(vf);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int res = (findAluFlagValue(vf, z, Alu.Flag.Z) ? Alu.Flag.Z.mask() : 0)
                + (findAluFlagValue(vf, n, Alu.Flag.N) ? Alu.Flag.N.mask() : 0)
                + (findAluFlagValue(vf, h, Alu.Flag.H) ? Alu.Flag.H.mask() : 0)
                + (findAluFlagValue(vf, c, Alu.Flag.C) ? Alu.Flag.C.mask() : 0);
        regFile.set(Reg.F, res);
    }

    private boolean findAluFlagValue(int vf, FlagSrc src, Alu.Flag flag) {
        switch (src) {
            case V0:  return false;
            case V1:  return true;
            case ALU: return Bits.test(Alu.unpackFlags(vf), flag.index());
            case CPU: return Bits.test(regFile.get(Reg.F), flag.index());
        }
        throw new NullPointerException();
    }
    
    /******* Interruptions Functions **************/
    
    //return true if the CPU must go directly to the next cycle
    private boolean handleInterrupts(long cycle){
        if (detectInterrupts() != null) {
            Interrupt curInterrupt = detectInterrupts();
            if (IME) {
                IF &= ~curInterrupt.mask();
                IME = false;
                push16(PC);
                PC = AddressMap.INTERRUPTS[curInterrupt.index()];
                nextNonIdleCycle = cycle + CYCLES_AFTER_DETECT_INTERRUPT;
                return true;
            } else {
                nextNonIdleCycle = cycle;
            }
        }
        return false;
    }
    
    private Interrupt detectInterrupts() {
        if ((IME || nextNonIdleCycle == Long.MAX_VALUE) && (IE & IF) != 0)
            return Interrupt.values()[Integer.numberOfTrailingZeros(Integer.lowestOneBit(IE & IF))];
        else return null;
    }
    
    //This function was used for JUnit tests
    public int[] _testGetPcSpAFBCDEHL() {
        int[] res = new int[10];
        res[0] = PC;
        res[1] = SP;
        res[2] = regFile.get(Reg.A);
        res[3] = regFile.get(Reg.F);
        res[4] = regFile.get(Reg.B);
        res[5] = regFile.get(Reg.C);
        res[6] = regFile.get(Reg.D);
        res[7] = regFile.get(Reg.E);
        res[8] = regFile.get(Reg.H);
        res[9] = regFile.get(Reg.L);
        return res;
    }
}
