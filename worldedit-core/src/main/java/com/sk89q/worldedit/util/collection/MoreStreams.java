/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.collection;

import com.google.common.collect.AbstractIterator;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * Additional stream facilities.
 */
public class MoreStreams {

    /**
     * Emit elements from {@code stream} until {@code predicate} returns {@code false}.
     */
    public static <T> Stream<T> takeWhile(Stream<T> stream, Predicate<T> predicate) {
        return takeUntil(stream, predicate.negate());
    }

    /**
     * Emit elements from {@code stream} until {@code predicate} returns {@code true}.
     */
    public static <T> Stream<T> takeUntil(Stream<T> stream, Predicate<T> predicate) {
        Spliterator<T> spliterator = stream.spliterator();
        Iterator<T> iter = new AbstractIterator<T>() {

            private Iterator<T> source = Spliterators.iterator(spliterator);

            @Override
            protected T computeNext() {
                Iterator<T> src = requireNonNull(source);
                if (!src.hasNext()) {
                    return done();
                }
                T next = src.next();
                if (predicate.test(next)) {
                    return done();
                }
                return next;
            }

            private T done() {
                // allow GC of source
                source = null;
                return endOfData();
            }
        };
        int chars = spliterator.characteristics();
        // Not SIZED, Not SUBSIZED
        chars &= ~(Spliterator.SIZED | Spliterator.SUBSIZED);
        return StreamSupport.stream(Spliterators.spliterator(
            iter, spliterator.estimateSize(), chars
        ), stream.isParallel()).onClose(stream::close);
    }

    /**
     * Create a stream of every element in the given buffer view, from it's current position to it's current limit.
     * The position of the buffer is not modified.
     *
     * <p>The bytes are sign-extended to be ints. This means {@code -1} will still be {@code -1}.</p>
     *
     * @param buffer the buffer to stream
     */
    public static IntStream bufferStream(ByteBuffer buffer) {
        return IntStream.range(buffer.position(), buffer.limit()).map(buffer::get);
    }

    /**
     * Create a stream of every element in the given buffer view, from it's current position to it's current limit.
     * The position of the buffer is not modified.
     *
     * @param buffer the buffer to stream
     */
    public static IntStream bufferStream(IntBuffer buffer) {
        return IntStream.range(buffer.position(), buffer.limit()).map(buffer::get);
    }

    /**
     * Create a stream of every element in the given buffer view, from it's current position to it's current limit.
     * The position of the buffer is not modified.
     *
     * @param buffer the buffer to stream
     */
    public static LongStream bufferStream(LongBuffer buffer) {
        return IntStream.range(buffer.position(), buffer.limit()).mapToLong(buffer::get);
    }

    private MoreStreams() {
    }
}
