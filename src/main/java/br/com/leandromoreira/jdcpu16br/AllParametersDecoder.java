package br.com.leandromoreira.jdcpu16br;

public class AllParametersDecoder {

    private static final int NUMBER_OF_DECODERS = 0x40;
    private static final int POP = 0x18;
    private static final int PEEK = 0x19;
    private static final int PUSH = 0x1A;
    private static final int SP_DECODER = 0x1B;
    private static final int PC_DECODER = 0x1C;
    private static final int O_DECODER = 0x1D;
    private static final int NEXT_WORD_INDIRECT = 0x1E;
    private static final int NEXT_WORD = 0x1F;
    private final CPU cpu;

    public AllParametersDecoder(final CPU cpu) {
        this.cpu = cpu;
    }

    public ParameterDecoder[] all() {
        final ParameterDecoder[] decoder = new ParameterDecoder[NUMBER_OF_DECODERS];
        fillDecoderDirectRegister(decoder);
        fillDecoderIndirectRegister(decoder);
        fillDecoderIndirectNextWordPlusRegister(decoder);
        fillDecoderLiteral(decoder);
        decoder[POP] = new ParameterDecoder(POP) {

            @Override
            public void write(final int value) {
            }

            @Override
            public int read() {
                return cpu.memory().readFrom(cpu.getStackPointerAndIncrement());
            }

            @Override
            public String type() {
                return "POP";
            }
        };
        decoder[PEEK] = new ParameterDecoder(PEEK) {

            @Override
            public void write(final int value) {
            }

            @Override
            public int read() {
                return cpu.memory().readFrom(cpu.getStackPointer());
            }

            @Override
            public String type() {
                return "PEEK";
            }
        };
        decoder[PUSH] = new ParameterDecoder(PUSH) {

            @Override
            public void write(final int value) {
            }

            @Override
            public int read() {
                return cpu.memory().readFrom(cpu.getStackPointerAndDecrement());
            }

            @Override
            public String type() {
                return "PUSH";
            }
        };
        decoder[SP_DECODER] = new ParameterDecoder(SP_DECODER) {

            @Override
            public void write(final int value) {
                cpu.setStackPointer(value);
            }

            @Override
            public int read() {
                return cpu.getStackPointer();
            }

            @Override
            public String type() {
                return "SP";
            }
        };
        decoder[PC_DECODER] = new ParameterDecoder(PC_DECODER) {

            @Override
            public void write(final int value) {
                cpu.setProgramCounter(value);
            }

            @Override
            public int read() {
                return cpu.getProgramCounter();
            }

            @Override
            public String type() {
                return "PC";
            }
        };
        decoder[O_DECODER] = new ParameterDecoder(O_DECODER) {

            @Override
            public void write(final int value) {
                cpu.setOverflow(value);
            }

            @Override
            public int read() {
                return cpu.getOverflow();
            }

            @Override
            public String type() {
                return "O";
            }
        };
        decoder[NEXT_WORD_INDIRECT] = new ParameterDecoder(NEXT_WORD_INDIRECT) {

            @Override
            public void write(final int value) {
                cpu.memory().writeAt(cpu.memory().readFrom(getMyProgramCounter()+1), value);
            }

            @Override
            public int read() {
                return cpu.memory().readFrom(cpu.memory().readFrom(getMyProgramCounter()+1));
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public String type() {
                return "NEXT_WORD_INDIRECT";
            }
        };
        decoder[NEXT_WORD] = new ParameterDecoder(NEXT_WORD) {

            @Override
            public void write(final int value) {
            }

            @Override
            public int read() {
                return cpu.memory().readFrom(getMyProgramCounter()+1);
            }

            @Override
            public int size() {
                return 1;
            }
            
            @Override
            public String type() {
                return "NEXT_WORD";
            }
        };
        return decoder;
    }

    private void fillDecoderDirectRegister(final ParameterDecoder[] decoder) {
        for (int registerIndex = 0x00; registerIndex <= 0x07; registerIndex++) {
            decoder[registerIndex] = new ParameterDecoder(registerIndex) {

                @Override
                public void write(int value) {
                    cpu.setRegister(index, value);
                }

                @Override
                public int read() {
                    return cpu.register(index);
                }

                @Override
                public String type() {
                    return "DIRECT_REGISTER";
                }
            };
        }
    }

    private void fillDecoderIndirectRegister(final ParameterDecoder[] decoder) {
        for (int registerIndex = 0x08; registerIndex <= 0x0F; registerIndex++) {
            decoder[registerIndex] = new ParameterDecoder(registerIndex) {

                @Override
                public void write(int value) {
                    cpu.memory().writeAt(cpu.register(index - 0x8), value);
                }

                @Override
                public int read() {
                    return cpu.memory().readFrom(cpu.register(index - 0x8));
                }

                @Override
                public String type() {
                    return "INDIRECT_REGISTER";
                }
            };
        }
    }

    private void fillDecoderLiteral(final ParameterDecoder[] decoder) {
        for (int registerIndex = 0x20; registerIndex <= 0x3F; registerIndex++) {
            decoder[registerIndex] = new ParameterDecoder(registerIndex) {

                @Override
                public void write(int value) {
                }

                @Override
                public int read() {
                    return index - 0x20;
                }

                @Override
                public String type() {
                    return "LITERAL";
                }
            };
        }
    }

    private void fillDecoderIndirectNextWordPlusRegister(final ParameterDecoder[] decoder) {
        for (int registerIndex = 0x10; registerIndex <= 0x17; registerIndex++) {
            decoder[registerIndex] = new ParameterDecoder(registerIndex) {

                @Override
                public void write(int value) {
                    cpu.memory().writeAt(nextWordPlusRegister(), value);
                }

                @Override
                public int read() {
                    return cpu.memory().readFrom(nextWordPlusRegister());
                }

                private int nextWordPlusRegister() {
                    return cpu.memory().readFrom(cpu.getNextProgramCounter()) + cpu.register(index);
                }

                @Override
                public int size() {
                    return 1;
                }

                @Override
                public String type() {
                    return "INDIRECT_NEXT_WORD_PLUS_REGISTER";
                }
            };
        }
    }
}
