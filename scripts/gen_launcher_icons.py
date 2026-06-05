"""Genera le icone launcher da una sorgente PNG.

Produce:
- mipmap-<density>/ic_launcher.png e ic_launcher_round.png  (fallback legacy, su fondo blu)
- drawable/ic_launcher_foreground_img.png  (foreground adaptive, contenuto nella safe-zone)

Uso: python scripts/gen_launcher_icons.py <png_sorgente>
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw

# Blu del brand (campionato dalla squircle dell'icona)
BRAND_BLUE = (34, 113, 202, 255)
# Densita' launcher quadrato (px lato)
LEGACY_SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
# Foreground adaptive a 108dp: una sola immagine ad alta densita', scalata dal sistema
FG_CANVAS = 432
# Frazione della tela occupata dal contenuto (ingrandita per coprire i bordi chiari)
FG_CONTENT = 0.86
# Frazione occupata nel launcher quadrato legacy (overscan: copre gli angoli chiari)
LEGACY_CONTENT = 1.06


def trim_to_content(src: Image.Image) -> Image.Image:
    """Ritaglia il padding chiaro attorno alla squircle dell'icona."""
    bg = Image.new("RGBA", src.size, (244, 246, 251, 255))
    from PIL import ImageChops
    diff = ImageChops.difference(src.convert("RGBA"), bg).convert("L")
    bbox = diff.point(lambda p: 255 if p > 44 else 0).getbbox()
    cropped = src.crop(bbox) if bbox else src
    side = max(cropped.size)
    square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    square.paste(cropped, ((side - cropped.width) // 2, (side - cropped.height) // 2))
    return square


def scaled_centered(content: Image.Image, canvas: int, fraction: float) -> Image.Image:
    target = int(canvas * fraction)
    img = content.resize((target, target), Image.LANCZOS)
    out = Image.new("RGBA", (canvas, canvas), (0, 0, 0, 0))
    off = (canvas - target) // 2
    out.paste(img, (off, off), img)
    return out


def main() -> None:
    src_path = Path(sys.argv[1])
    res_dir = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"
    content = trim_to_content(Image.open(src_path).convert("RGBA"))

    # Foreground adaptive (su sfondo trasparente, contenuto nella safe-zone)
    fg = scaled_centered(content, FG_CANVAS, FG_CONTENT)
    (res_dir / "drawable").mkdir(parents=True, exist_ok=True)
    fg.save(res_dir / "drawable" / "ic_launcher_foreground_img.png")
    print("drawable/ic_launcher_foreground_img.png ok")

    # Legacy quadrato/tondo: contenuto su fondo blu pieno
    for density, size in LEGACY_SIZES.items():
        out_dir = res_dir / f"mipmap-{density}"
        base = Image.new("RGBA", (size, size), BRAND_BLUE)
        c = content.resize(
            (int(size * LEGACY_CONTENT), int(size * LEGACY_CONTENT)), Image.LANCZOS
        )
        off = (size - c.width) // 2
        base.paste(c, (off, off), c)
        base.save(out_dir / "ic_launcher.png")

        mask = Image.new("L", (size, size), 0)
        ImageDraw.Draw(mask).ellipse((0, 0, size - 1, size - 1), fill=255)
        rnd = base.copy()
        rnd.putalpha(mask)
        rnd.save(out_dir / "ic_launcher_round.png")
        print(f"mipmap-{density}: {size}px ok")


if __name__ == "__main__":
    main()
