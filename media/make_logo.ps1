Add-Type -AssemblyName System.Drawing

$outDir = ".\media"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

function New-RoundedPath([float]$x, [float]$y, [float]$w, [float]$h, [float]$r) {
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d = 2 * $r
    $p.AddArc($x, $y, $d, $d, 180, 90)
    $p.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $p.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $p.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $p.CloseFigure()
    return $p
}

function New-HeartPath([float]$cx, [float]$cy, [float]$r) {
    $p = New-Object System.Drawing.Drawing2D.GraphicsPath
    $half = [int]($r / 1.2)
    $cirY = $cy - $half
    $p.AddEllipse([float]($cx - $r - $half), $cirY, [float](2 * $r), [float](2 * $r))
    $p.AddEllipse([float]($cx + $half - $r), $cirY, [float](2 * $r), [float](2 * $r))
    $rot = New-Object System.Drawing.RectangleF([float]($cx - $half), [float]($cy - $half), [float](2 * $half), [float](2 * $half))
    $p.AddPolygon([System.Drawing.PointF[]]@(
        (New-Object System.Drawing.PointF($cx, [float]($cy + $half))),
        (New-Object System.Drawing.PointF([float]($cx - $half), $cy)),
        (New-Object System.Drawing.PointF($cx, [float]($cy - $half))),
        (New-Object System.Drawing.PointF([float]($cx + $half), $cy))
    ))
    $p.CloseFigure()
    return $p
}

$bmp = New-Object System.Drawing.Bitmap 512, 512
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.Clear([System.Drawing.Color]::Transparent)

$cream  = [System.Drawing.Color]::FromArgb(255, 253, 246, 238)
$terra  = [System.Drawing.Color]::FromArgb(255, 166, 83, 59)
$terraD = [System.Drawing.Color]::FromArgb(255, 124, 58, 40)
$saffron = [System.Drawing.Color]::FromArgb(255, 242, 193, 78)

$brCream = New-Object System.Drawing.SolidBrush $cream
$brTerra = New-Object System.Drawing.SolidBrush $terra
$brSaff  = New-Object System.Drawing.SolidBrush $saffron

$g.FillPath($brCream, (New-RoundedPath 16 16 480 480 110))

$g.FillPath($brTerra, (New-RoundedPath 120 150 272 250 44))

$flag = New-Object System.Drawing.Drawing2D.GraphicsPath
$flag.AddPolygon([System.Drawing.Point[]]@(
    (New-Object System.Drawing.Point 348, 82),
    (New-Object System.Drawing.Point 436, 82),
    (New-Object System.Drawing.Point 436, 188),
    (New-Object System.Drawing.Point 392, 154),
    (New-Object System.Drawing.Point 348, 188)
))
$flag.CloseFigure()
$g.FillPath($brSaff, $flag)

$g.FillPath($brCream, (New-HeartPath 256 268 46))

$font = New-Object System.Drawing.Font("Segoe UI", 50, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$sf = New-Object System.Drawing.StringFormat
$sf.Alignment = [System.Drawing.StringAlignment]::Center
$sf.LineAlignment = [System.Drawing.StringAlignment]::Center
$rect = New-Object System.Drawing.RectangleF(16, 404, 480, 76)
$g.DrawString("CraftKart", $font, $brTerra, $rect, $sf)

$small = New-Object System.Drawing.Font("Segoe UI", 20, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$sub = New-Object System.Drawing.RectangleF(16, 474, 480, 26)
$g.DrawString("HANDMADE & POS", $small, (New-Object System.Drawing.SolidBrush $terraD), $sub, $sf)

$logo = Join-Path $outDir "craftkart-logo.png"
$bmp.Save($logo, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()
Get-Item $logo | Select-Object Name, @{n='KB'; e={[math]::Round($_.Length / 1KB, 1)}}
