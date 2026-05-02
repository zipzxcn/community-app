import type { LocationQuery, LocationQueryRaw } from 'vue-router'
import type { PostQuery } from '@/types/post'

const FEED_SCROLL_STORAGE_KEY = 'community.feed.scroll'

export interface FeedRouteState {
  page: number
  size: number
  sort: 'latest' | 'hot'
  keyword: string
  tagId?: number
}

export function parseFeedRouteQuery(query: LocationQuery): FeedRouteState {
  const page = Number(Array.isArray(query.page) ? query.page[0] : query.page)
  const size = Number(Array.isArray(query.size) ? query.size[0] : query.size)
  const sortValue = Array.isArray(query.sort) ? query.sort[0] : query.sort
  const keywordValue = Array.isArray(query.keyword) ? query.keyword[0] : query.keyword
  const tagIdValue = Number(Array.isArray(query.tagId) ? query.tagId[0] : query.tagId)

  return {
    page: Number.isFinite(page) && page > 0 ? page : 1,
    size: Number.isFinite(size) && size > 0 ? size : 10,
    sort: sortValue === 'hot' ? 'hot' : 'latest',
    keyword: keywordValue?.trim() || '',
    tagId: Number.isFinite(tagIdValue) && tagIdValue > 0 ? tagIdValue : undefined,
  }
}

export function buildFeedRouteQuery(state: Partial<PostQuery> & { restoreFeed?: boolean }): LocationQueryRaw {
  return {
    page: String(state.page ?? 1),
    size: String(state.size ?? 10),
    sort: state.sort === 'hot' ? 'hot' : 'latest',
    keyword: state.keyword?.trim() || undefined,
    tagId: state.tagId ? String(state.tagId) : undefined,
    restoreFeed: state.restoreFeed ? '1' : undefined,
  }
}

export function createFeedStateKey(state: Pick<FeedRouteState, 'page' | 'size' | 'sort' | 'keyword' | 'tagId'>) {
  return JSON.stringify({
    page: state.page,
    size: state.size,
    sort: state.sort,
    keyword: state.keyword?.trim() || '',
    tagId: state.tagId || 0,
  })
}

export function saveFeedScroll(state: FeedRouteState, scrollY: number) {
  sessionStorage.setItem(
    FEED_SCROLL_STORAGE_KEY,
    JSON.stringify({
      key: createFeedStateKey(state),
      scrollY: Math.max(scrollY, 0),
      savedAt: Date.now(),
    }),
  )
}

export function loadFeedScroll(state: FeedRouteState) {
  const raw = sessionStorage.getItem(FEED_SCROLL_STORAGE_KEY)
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as { key?: string; scrollY?: number }
    if (parsed.key !== createFeedStateKey(state)) {
      return null
    }
    return typeof parsed.scrollY === 'number' ? parsed.scrollY : null
  } catch {
    return null
  }
}

export function clearFeedScroll() {
  sessionStorage.removeItem(FEED_SCROLL_STORAGE_KEY)
}
