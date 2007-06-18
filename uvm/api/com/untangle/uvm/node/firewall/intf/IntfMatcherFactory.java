/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc. 
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module.  An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.untangle.uvm.node.firewall.intf;

import com.untangle.uvm.IntfEnum;

import com.untangle.uvm.node.ParseException;

import com.untangle.uvm.node.firewall.ParsingFactory;


/**
 * A factory for interface matchers.
 *
 * @author <a href="mailto:rbscott@untangle.com">Robert Scott</a>
 * @version 1.0
 */
public class IntfMatcherFactory
{
    private static final IntfMatcherFactory INSTANCE = new IntfMatcherFactory();

    /** The parser used to translate strings into IntfDBMatchers. */
    private final ParsingFactory<IntfDBMatcher> parser;

    private IntfMatcherFactory()
    {
        this.parser = new ParsingFactory<IntfDBMatcher>( "intf matcher" );
        this.parser.registerParsers( IntfSimpleMatcher.PARSER, IntfSingleMatcher.PARSER, 
                                     IntfInverseMatcher.PARSER, IntfSetMatcher.PARSER );
    }

    public IntfDBMatcher getAllMatcher() 
    {
        return IntfSimpleMatcher.getAllMatcher();
    }

    public IntfDBMatcher getNilMatcher() 
    {
        return IntfSimpleMatcher.getNilMatcher();
    }

    public IntfDBMatcher getExternalMatcher()
    {
        return IntfSingleMatcher.getExternalMatcher();
    }
    
    public IntfDBMatcher getInternalMatcher()
    {
        return IntfSingleMatcher.getInternalMatcher();
    }

    public IntfDBMatcher getDmzMatcher()
    {
        return IntfSingleMatcher.getDmzMatcher();
    }

    public IntfDBMatcher getVpnMatcher()
    {
        return IntfSingleMatcher.getVpnMatcher();
    }
    
    /**
     * Update the enumeration of IntfMatchers.
     *
     * @param intfEnum The new interface enumeration.
     */
    public void updateEnumeration( IntfEnum intfEnum )
    {
        IntfMatcherEnumeration.getInstance().updateEnumeration( intfEnum );
    }

    /**
     * Retrieve the enumeration of possible IntfMatchers.
     *
     * @return An array of valid IntfMatchers.
     */    
    public IntfDBMatcher[] getEnumeration()
    {
        return IntfMatcherEnumeration.getInstance().getEnumeration();
    }

    /**
     * Retrieve the default IntfMatcher.
     *
     * @return The default IntfMatcher
     */
    public IntfDBMatcher getDefault()
    {
        return IntfMatcherEnumeration.getInstance().getDefault();
    }

    /**
     * Retrieve an intf matcher that matches <param>intf</param>
     *
     * @param intf The interface to match.
     */
    public IntfDBMatcher makeSingleMatcher( byte intf ) throws ParseException
    {
        return IntfSingleMatcher.makeInstance( intf );
    }

    /**
     * Retrieve an intf matcher that matches any of the interfaces in
     * <param>intfArray</param>
     *
     * @param intfArray An array of interfaces to match.
     */
    public IntfDBMatcher makeSetMatcher( byte ... intfArray ) throws ParseException
    {
        switch ( intfArray.length ) {
        case 0: return IntfSimpleMatcher.getNilMatcher();
        case 1: return makeSingleMatcher( intfArray[0] );
        default: return IntfSetMatcher.makeInstance( intfArray );
        }
    }

    /**
     * Retrieve an intf matcher that doesn't match any of the
     * interfaces in <param>intfArray</param>
     *
     * @param intfArray Array of interfaces that shouldn't match.
     */
    public IntfDBMatcher makeInverseMatcher( byte ... intfArray ) throws ParseException
    {
        switch ( intfArray.length ) {
        case 0:  return IntfSimpleMatcher.getAllMatcher();
        default: return IntfInverseMatcher.makeInstance( intfArray );
        }
    }

    /**
     * Convert <param>value</param> to an IntfDBMatcher.
     *
     * @param value The string to parse.
     */
    public static IntfDBMatcher parse( String value ) throws ParseException
    {
        return INSTANCE.parser.parse( value );
    }

    public static IntfMatcherFactory getInstance()
    {
        return INSTANCE;
    }
}

