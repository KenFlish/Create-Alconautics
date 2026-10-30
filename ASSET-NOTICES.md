# Models and textures

The original Blockbench files in `art/source` were supplied by the user for this project. The single and paired bottle models are the updated user-supplied `samogon.bbmodel` and `2samodona.bbmodel`; their original files are preserved unchanged.

The dry chamber uses a native Minecraft JSON model with its original UV layout. The head, condenser and bottles are deterministically converted to OBJ to preserve rotations and glass faces that ordinary JSON cuboids cannot represent; UV orientation is explicitly corrected for the NeoForge OBJ loader. The paired bottle geometry is split into two independently animated parts. Bottle wobble and overlays use the separately installed Foundation library, as Brewery does. No source models or textures from Brewery are redistributed.

Unmodified stock Create textures are referenced by resource identifier (`create:block/...`) and resolved from the separately installed mod. Their bytes are compared before reusing a stock reference: modified textures supplied in the user's models are preserved instead of silently replaced. The updated condenser uses the packed texture embedded in the supplied Blockbench file, without an Aeronautics dependency. The supplied `moonshine.png` is used unchanged as the hand/inventory sprite; the placed bottle retains the supplied 3D geometry and its separate texture.

The Create boiler gauge models are rendered directly from installed Create, without copying its models. The code integrates with Create under its MIT terms; copyright notice: Copyright (c) The Create Team / The Creators of Create. See https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/LICENSE.md for the original license and asset restrictions.

Dependency JARs in `libs/` are only development inputs and are not embedded in the distributable JAR.

The optional Sable integration uses its collision callback API. Sable and its companion/Veil/physics libraries are compile-time inputs and are not redistributed in this mod. The High Spirits effect references the installed vanilla Strength icon through an atlas alias; no Minecraft texture bytes are copied.

The two Ponder structures were supplied by the user (`ponder1_1.nbt` and `ponder2.nbt`). Original files are retained in `art/source`; the packaged scene copies remove saved block-entity world coordinates and owner identifiers. Tutorial scripts add the preparation steps while preserving the supplied layouts.

The growth/shrinking effect icons were supplied by the user as `+.png` and `-.png` and are retained unchanged as `art/source/growth_icon.png` and `shrinking_icon.png`.

No film music, instrumental extraction, or music disc is included in this version. The previously bundled recording was removed pending permission from the rights holders.
