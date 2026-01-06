/**
 * This file is part of ViewCarousel.
 * <p>
 * ViewCarousel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * ViewCarousel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with ViewCarousel. If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.wandomium.viewcarousel.ui;

@FunctionalInterface
public interface ICaptureInput
{
    /**
     * @param captureReq requested capture state
     * @return actual capture state of the component. Can be rejected if unsupported
     */
    boolean setCaptureInput(final boolean captureReq);
}
