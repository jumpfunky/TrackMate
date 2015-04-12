package fiji.plugin.trackmate.interactivetests;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import io.scif.img.ImgIOException;

import java.io.File;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

public class Imglib2TopHat_TestDrive {

	public static <T extends RealType<T> & NativeType<T>> void main(final String[] args) throws ImgIOException, InterruptedException {

		ImageJ.main(args);
		final File file = new File("/Users/tinevez/Desktop/Data/Uneven.tif");
		final ImagePlus imp = IJ.openImage(file.getAbsolutePath());
		final Img<T> img = ImagePlusAdapter.wrap(imp);

		final long start = System.currentTimeMillis();

		final Shape shape = new RectangleShape(3, false);
		final Img<T> target = dilate(img, shape);

		final long end = System.currentTimeMillis();

		System.out.println("Processing done in " + (end - start) + " ms.");// DEBUG

		ImageJFunctions.show(img);
		ImageJFunctions.show(target);

	}

	public static <T extends RealType<T> & NativeType<T>> Img<T> dilate(final Img<T> img, final Shape shape) {

		final Img<T> target = img.factory().create(img, img.firstElement().copy());
		final RandomAccess<T> ra = target.randomAccess();

		final T max = img.firstElement().createVariable();
		final IterableInterval<Neighborhood<T>> neighborhoods = shape.neighborhoods(img);
		for (final Neighborhood<T> neighborhood : neighborhoods) {

			final Cursor<T> cursor = neighborhood.cursor();
			max.setReal(max.getMinValue());

			while (cursor.hasNext()) {

				cursor.fwd();
				if (!Intervals.contains(img, cursor)) {
					continue;
				}

				final T val = cursor.get();
				if (val.compareTo(max) > 0) {
					max.set(val);
				}
			}

			ra.setPosition(neighborhood);
			ra.get().set(max);
		}

		return target;
	}
}
