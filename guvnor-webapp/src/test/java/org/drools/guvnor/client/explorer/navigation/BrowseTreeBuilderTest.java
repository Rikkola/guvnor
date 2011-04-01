/*
 * Copyright 2011 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.drools.guvnor.client.explorer.navigation;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class BrowseTreeBuilderTest {

    private BrowseTreeBuilder builder;
    private BrowseHeaderView stackItemHeaderView;
    private BrowseTreeView browseTreeView;

    @Before
    public void setUp() throws Exception {
        builder = new BrowseTreeBuilder();
        AuthorNavigationViewFactory authorNavigationViewFactory = setUpNavigationFactory();
        stackItemHeaderView = setUpHeaderView(authorNavigationViewFactory);
        browseTreeView = setUpContentView(authorNavigationViewFactory);
    }

    @Test
    public void testBuildPermission() throws Exception {
        assertTrue(builder.hasPermissionToBuild());
    }

    @Test
    public void testCheckHeader() throws Exception {
        assertEquals(stackItemHeaderView, builder.getHeader());
    }

    @Test
    public void testCheckContent() throws Exception {
        builder.createNewBrowseTree();
        assertEquals(browseTreeView, builder.getContent());

        verify(browseTreeView, never()).addFind();
        verify(browseTreeView, never()).addRootStateTreeItem();
        verify(browseTreeView, never()).addRootCategoryTreeItem();
    }

    // TODO: the following is too fancy, what is built into the tree item should be decided in the item -Rikkola-
//    @Test
//    public void testBuildFind() throws Exception {
//        builder.createNewBrowseTree();
//        builder.buildFind();
//        assertEquals(browseTreeView, builder.getContent());
//
//        verify(browseTreeView).addFind();
//        verify(browseTreeView, never()).addRootStateTreeItem();
//        verify(browseTreeView, never()).addRootCategoryTreeItem();
//    }
//
//    @Test
//    public void testBuildStateTreeItem() throws Exception {
//        builder.createNewBrowseTree();
//        builder.buildStateTreeItem();
//        assertEquals(browseTreeView, builder.getContent());
//
//        verify(browseTreeView, never()).addFind();
//        verify(browseTreeView).addRootStateTreeItem();
//        verify(browseTreeView, never()).addRootCategoryTreeItem();
//    }
//
//    @Test
//    public void testBuildCategoriesTreeItem() throws Exception {
//        builder.createNewBrowseTree();
//        builder.buildCategoriesTreeItem();
//        assertEquals(browseTreeView, builder.getContent());
//
//        verify(browseTreeView, never()).addFind();
//        verify(browseTreeView, never()).addRootStateTreeItem();
//        verify(browseTreeView).addRootCategoryTreeItem();
//    }

    private AuthorNavigationViewFactory setUpNavigationFactory() {
        AuthorNavigationViewFactory authorNavigationViewFactory = mock(AuthorNavigationViewFactory.class);
        builder.setViewFactory(authorNavigationViewFactory);
        return authorNavigationViewFactory;
    }

    private BrowseTreeView setUpContentView(AuthorNavigationViewFactory authorNavigationViewFactory) {
        BrowseTreeView browseTreeView = mock(BrowseTreeView.class);
        when(authorNavigationViewFactory.getBrowseTreeView()).thenReturn(browseTreeView);
        return browseTreeView;
    }

    private BrowseHeaderView setUpHeaderView(AuthorNavigationViewFactory authorNavigationViewFactory) {
        BrowseHeaderView stackItemHeaderView = mock(BrowseHeaderView.class);
        when(authorNavigationViewFactory.getBrowseHeaderView()).thenReturn(stackItemHeaderView);
        return stackItemHeaderView;
    }
}
