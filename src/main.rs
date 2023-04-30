#![no_std]
#![no_main]

extern crate panic_halt;

use core::arch::asm;

use riscv_rt::entry;

/*
/// 4 ways to do a+b
fn normal_add(a: i32, mut b: i32) -> i32 {
    //unsafe { asm!(".word 0x00b50533") };
    //.insn r opcode,func3,func7,rd,rs1,rs2
    unsafe { asm!(".insn r 0x33,0x0,0x0,{0},{0},{1}", inout(reg) b, in(reg) a) };
    //unsafe { asm!("add {0},{0},{1}", inout(reg) b, in(reg) a) };
    b
    //a+b
}*/

fn simd_add(a: i32, b: i32) -> i32 {
    let ret;
    //.insn r opcode,func3,func7,rd,rs1,rs2
    unsafe { asm!(".insn r 0x33,0x0,0x3,{0},{1},{2}",
        out(reg) ret,
        in(reg) a,
        in(reg) b,
        options(pure, nomem),//if without this option, 0x4001_0600 and 0x4001_0700 will be load a second time before normal add
    ) };
    //unsafe { asm!(".insn r 0x33,0x0,0x3,{0},{1},{2}", out(reg) ret, in(reg) a, in(reg) b) };
    ret
}

// use `main` as the entry point of this application
// `main` is not allowed to return
#[entry]
fn main() -> ! {
    let a = 0x4001_0600 as *const i32;
    let b = 0x4001_0700 as *const i32;
    let simd = unsafe { simd_add(*a, *b) };
    let normal = unsafe { *a + *b };
    let simd_mem = 0x4001_0800 as *mut i32;
    let normal_mem = 0x4001_0900 as *mut i32;
    unsafe { *simd_mem = simd };
    unsafe { *normal_mem = normal };
    loop { }
}
