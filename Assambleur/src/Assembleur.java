import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class Assembleur {
    static HashMap<String, Integer> instructions = new HashMap<>();
    static{
        instructions.put("ADD", 0);
        instructions.put("SUB", 1);
        instructions.put("MUL", 2);
        instructions.put("AND", 3);
        instructions.put("OR",  4);
        instructions.put("XOR", 5);
        instructions.put("SL",  6);
        instructions.put("SR",  7);
        instructions.put("LD",  0);
        instructions.put("ST",  1);

    }
    public static void main(String[] args) {
        if (args.length > 0) {
            File asm = new File(args[0]);
            try(Scanner ligneScanner = new Scanner(asm)){
                while (ligneScanner.hasNextLine()){
                    String line = ligneScanner.nextLine();
                    if (!line.trim().isEmpty()) {
                        int code = assembler(line);
                        System.out.printf("%08x\n", code);
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Erreur de fichier.");
            }
        }
    }

    public static int assembler(String line) {
        String[] mots = line.replace(",", " ").trim().split("\\s+");
        String instruction = mots[0].toUpperCase();

        int type = 0, opcode = 0, imm = 0;
        int dr = 0, sr1 = 0, sr2 = 0, cst = 0;

        if (instruction.endsWith("I")) {
            imm = 1;
            instruction = instruction.substring(0, instruction.length() - 1);
        }

        switch(instruction) {
            case "ADD":
            case "SUB":
            case "AND":
            case "OR":
            case "XOR":
            case "SL":
            case "SR":
            case "MUL":
                type = 0b00; // Type UAL
                opcode = instructions.get(instruction);
                dr = getReg(mots[1]);
                sr1 = getReg(mots[2]);

                if (imm == 1) {
                    cst = Integer.parseInt(mots[3]) & 0xFFFF;
                } else {
                    sr2 = getReg(mots[3]);
                }
                break;
            case "LD":
            case "ST":
                type = 0b01; //Type MEM
                opcode = instructions.get(instruction);
                dr  = getReg(mots[1]);
                sr1 = getReg(mots[2]);
                break;
            default:
                throw new IllegalArgumentException("Instruction inconnue : " + mots[0]);
        }

        // --- ASSEMBLAGE BINAIRE ---
        int resultat = 0;

        resultat |= (type & 0b11);              // Bits 0-1
        resultat |= (opcode & 0b111) << 2;      // Bits 2-4
        resultat |= (imm & 0b1) << 5;           // Bit 5
        resultat |= (dr & 0b111) << 6;          // Bits 6-8
        resultat |= (sr1 & 0b111) << 9;         // Bits 9-11
        resultat |= (sr2 & 0b111) << 12;        // Bits 12-14
        resultat |= (cst & 0xFFFF) << 16;       // Bits 16-31

        return resultat;
    }

    public static int getReg(String regStr) {
        int n = Integer.parseInt(regStr.toUpperCase().replace("R", "").trim());
        if (n < 0 || n > 7)
            throw new IllegalArgumentException("Registre invalide : " + regStr);
        return n;
    }
}