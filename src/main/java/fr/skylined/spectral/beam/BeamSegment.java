package fr.skylined.spectral.beam;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record BeamSegment(float start, float end, float wavelength) {
    public static final Codec<BeamSegment> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("s").forGetter(BeamSegment::start),
            Codec.FLOAT.fieldOf("e").forGetter(BeamSegment::end),
            Codec.FLOAT.fieldOf("w").forGetter(BeamSegment::wavelength)
    ).apply(inst, BeamSegment::new));

    public static final Codec<List<BeamSegment>> LIST_CODEC = CODEC.listOf();
}
