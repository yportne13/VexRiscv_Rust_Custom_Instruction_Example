MEMORY
{
  RAM : ORIGIN = 0x08000000, LENGTH = 16K
  FLASH : ORIGIN = 0x80000000, LENGTH = 16M
}

REGION_ALIAS("REGION_TEXT", FLASH);
REGION_ALIAS("REGION_RODATA", FLASH);
REGION_ALIAS("REGION_DATA", RAM);
REGION_ALIAS("REGION_BSS", RAM);
REGION_ALIAS("REGION_HEAP", RAM);
REGION_ALIAS("REGION_STACK", RAM);